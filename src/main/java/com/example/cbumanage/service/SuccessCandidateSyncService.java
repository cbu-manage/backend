package com.example.cbumanage.service;

import com.example.cbumanage.model.SuccessCandidate;
import com.example.cbumanage.repository.SuccessCandidateRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;

@Service
public class SuccessCandidateSyncService {

    @Autowired
    private SuccessCandidateRepository successCandidateRepository;

    @Value("${google.spreadSheet.key}")
    private String sheetKey;   // 구글 스프레드 시트 API 키

    @Value("${google.successSpreadSheet.id}")
    private String sheetId;    // 구글 스프레드 시트 ID

    // 시트 이름 설정
    private static final String RESULT_SHEET_NAME = "합불결과";
    private static final String APPLICANT_SHEET_NAME = "지원자목록";

    @Transactional
    public void syncSuccessCandidatesFromGoogleSheet() {
        // 1. 합불 결과 시트에서 "합격"인 닉네임 추출
        List<String> passedNicknames = getPassedNicknamesFromResultSheet();

        // 2. 지원자목록 시트에서 해당 닉네임의 지원자 정보 읽어오기
        List<SuccessCandidate> candidates = getCandidatesFromApplicantSheet(passedNicknames);

        // 3. DB 동기화 (신규 등록, 수정, 삭제)
        List<SuccessCandidate> dbCandidates = successCandidateRepository.findAll();
        Map<Long, SuccessCandidate> dbCandidateMap = new HashMap<>();
        for (SuccessCandidate candidate : dbCandidates) {
            dbCandidateMap.put(candidate.getStudentNumber(), candidate);
        }

        List<SuccessCandidate> toCreate = new ArrayList<>();
        List<SuccessCandidate> toUpdate = new ArrayList<>();

        for (SuccessCandidate candidate : candidates) {
            SuccessCandidate existing = dbCandidateMap.get(candidate.getStudentNumber());
            if (existing == null) {
                toCreate.add(candidate);
            } else if (hasChanged(existing, candidate)) {
                updateCandidateFields(existing, candidate);
                toUpdate.add(existing);
            }
            // 처리된 항목은 삭제
            dbCandidateMap.remove(candidate.getStudentNumber());
        }
        // 스프레드시트에는 없지만 DB에 남은 항목 삭제
        List<SuccessCandidate> toDelete = new ArrayList<>(dbCandidateMap.values());

        if (!toCreate.isEmpty()) {
            successCandidateRepository.saveAll(toCreate);
        }
        if (!toUpdate.isEmpty()) {
            successCandidateRepository.saveAll(toUpdate);
        }
        if (!toDelete.isEmpty()) {
            successCandidateRepository.deleteAll(toDelete);
        }
    }

    /**
     * 합불 결과 시트에서 헤더를 파싱하여 "최종결과" 열 인덱스를 찾고,
     * 해당 열의 값이 "합격"인 행에서 바로 왼쪽(닉네임 열)의 값을 추출합니다.
     */
    private List<String> getPassedNicknamesFromResultSheet() {
        URI sheetUri = getSheetUri(RESULT_SHEET_NAME);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<GoogleSheetResponse> response = rt.exchange(
                sheetUri,
                HttpMethod.GET,
                null,
                GoogleSheetResponse.class
        );
        GoogleSheetResponse sheetResponse = response.getBody();
        List<List<Object>> values = sheetResponse.getValues();
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }

        // 헤더 행에서 "최종결과" 열 인덱스를 찾음
        List<Object> header = values.get(0);
        int finalResultIndex = -1;
        for (int i = 0; i < header.size(); i++) {
            if ("최종결과".equals(header.get(i).toString().trim())) {
                finalResultIndex = i;
                break;
            }
        }
        if (finalResultIndex == -1) {
            throw new RuntimeException("합불 결과 시트에서 '최종결과' 열을 찾을 수 없습니다.");
        }
        int nickNameIndex = finalResultIndex - 1;
        if (nickNameIndex < 0) {
            throw new RuntimeException("합불 결과 시트에서 닉네임 열 인덱스가 잘못되었습니다.");
        }

        List<String> passedNicknames = new ArrayList<>();
        // 데이터 행 처리 (첫 행은 헤더)
        for (int i = 1; i < values.size(); i++) {
            List<Object> row = values.get(i);
            if (row.size() <= finalResultIndex) continue;
            String finalResult = row.get(finalResultIndex).toString().trim();
            if ("합격".equals(finalResult)) {
                String nick = row.size() > nickNameIndex ? row.get(nickNameIndex).toString().trim() : "";
                if (!nick.isEmpty()) {
                    passedNicknames.add(nick);
                }
            }
        }
        return passedNicknames;
    }

    /**
     * 지원자목록 시트에서 헤더를 파싱할 때,
     * 각 열 이름이 정확한 문자열이 아닌 긴 설명(예: "생년월일을 입력해주세요 ...")인 경우에도
     * 특정 키워드(예: "생년월일")가 포함되어 있는지 검사하여 인덱스 맵을 만듭니다.
     */
    private Map<String, Integer> buildColumnIndexMap(List<Object> header, Map<String, String> expectedColumns) {
        Map<String, Integer> columnIndexMap = new HashMap<>();
        for (int i = 0; i < header.size(); i++) {
            String headerName = header.get(i).toString().trim();
            for (Map.Entry<String, String> entry : expectedColumns.entrySet()) {
                String key = entry.getKey();               // 예: "생년월일"
                String expectedSubString = entry.getValue(); // 예: "생년월일"
                if (headerName.contains(expectedSubString)) {
                    columnIndexMap.put(key, i);
                    break; // 한 열에 대해 한 번 매칭되면 다음 header 셀로 넘어감
                }
            }
        }
        return columnIndexMap;
    }

    /**
     * 지원자목록 시트에서 "이름", "닉네임", "생년월일", "학년", "학번", "학과", "전화번호" 열의 인덱스를 찾아,
     * passedNicknames 목록에 해당하는 지원자 행만 SuccessCandidate 로 매핑합니다.
     * (단, 모델에는 생년월일 필드가 없으므로 해당 컬럼은 검증만 합니다.)
     */
    private List<SuccessCandidate> getCandidatesFromApplicantSheet(List<String> passedNicknames) {
        URI sheetUri = getSheetUri(APPLICANT_SHEET_NAME);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<GoogleSheetResponse> response = rt.exchange(
                sheetUri,
                HttpMethod.GET,
                null,
                GoogleSheetResponse.class
        );
        GoogleSheetResponse sheetResponse = response.getBody();
        List<List<Object>> values = sheetResponse.getValues();
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }

        // 헤더에서 필요한 열의 인덱스를 찾기 위한 기대값 (생년월일은 긴 설명 포함)
        List<Object> header = values.get(0);
        Map<String, String> expectedColumns = new HashMap<>();
        expectedColumns.put("이름", "이름");
        expectedColumns.put("닉네임", "닉네임");
        expectedColumns.put("생년월일", "생년월일");
        expectedColumns.put("학년", "학년");
        expectedColumns.put("학번", "학번");
        expectedColumns.put("학과", "학과");
        expectedColumns.put("전화번호", "전화번호");

        Map<String, Integer> columnIndexMap = buildColumnIndexMap(header, expectedColumns);

        // 모든 필수 열이 존재하는지 검증 (생년월일 열은 매핑에 사용되지는 않지만 존재해야 함)
        List<String> requiredColumns = Arrays.asList("이름", "닉네임", "생년월일", "학년", "학번", "학과", "전화번호");
        for (String col : requiredColumns) {
            if (!columnIndexMap.containsKey(col)) {
                throw new RuntimeException("지원자목록 시트에 '" + col + "' 열이 존재하지 않습니다.");
            }
        }

        List<SuccessCandidate> candidates = new ArrayList<>();
        // 헤더 이후의 데이터 행 처리
        for (int i = 1; i < values.size(); i++) {
            List<Object> row = values.get(i);
            int nickColIndex = columnIndexMap.get("닉네임");
            if (row.size() <= nickColIndex) continue;
            String nick = getStringValue(row, nickColIndex);
            if (passedNicknames.contains(nick)) {
                SuccessCandidate candidate = mapRowToSuccessCandidate(row, columnIndexMap);
                candidates.add(candidate);
            }
        }
        return candidates;
    }

    /**
     * row 데이터를 SuccessCandidate 객체로 매핑합니다.
     * 생년월일 열은 모델에 없으므로, 여기서는 이름, 닉네임, 학년, 학번, 학과, 전화번호만 매핑합니다.
     */
    private SuccessCandidate mapRowToSuccessCandidate(List<Object> row, Map<String, Integer> columnIndexMap) {
        SuccessCandidate candidate = new SuccessCandidate();
        candidate.setName(getStringValue(row, columnIndexMap.get("이름")));
        candidate.setNickName(getStringValue(row, columnIndexMap.get("닉네임")));
        candidate.setGrade(getStringValue(row, columnIndexMap.get("학년")));
        candidate.setMajor(getStringValue(row, columnIndexMap.get("학과")));
        candidate.setPhoneNumber(getStringValue(row, columnIndexMap.get("전화번호")));
        candidate.setStudentNumber(getLongValue(row, columnIndexMap.get("학번")));
        // 생년월일은 모델에 없으므로 따로 저장하지 않습니다.
        return candidate;
    }

    /**
     * DB에 저장된 후보와 스프레드시트의 후보 정보를 비교하여 변경되었는지 확인합니다.
     * (학번은 PK로 변경되지 않는다고 가정)
     */
    private boolean hasChanged(SuccessCandidate dbCandidate, SuccessCandidate sheetCandidate) {
        return !Objects.equals(dbCandidate.getName(), sheetCandidate.getName()) ||
                !Objects.equals(dbCandidate.getNickName(), sheetCandidate.getNickName()) ||
                !Objects.equals(dbCandidate.getGrade(), sheetCandidate.getGrade()) ||
                !Objects.equals(dbCandidate.getMajor(), sheetCandidate.getMajor()) ||
                !Objects.equals(dbCandidate.getPhoneNumber(), sheetCandidate.getPhoneNumber());
    }

    private void updateCandidateFields(SuccessCandidate existing, SuccessCandidate updated) {
        existing.setName(updated.getName());
        existing.setNickName(updated.getNickName());
        existing.setGrade(updated.getGrade());
        existing.setMajor(updated.getMajor());
        existing.setPhoneNumber(updated.getPhoneNumber());
    }

    private String getStringValue(List<Object> row, int index) {
        if (index >= row.size() || row.get(index) == null || row.get(index).toString().trim().isEmpty()) {
            return null;
        }
        return row.get(index).toString().trim();
    }

    private Long getLongValue(List<Object> row, int index) {
        String value = getStringValue(row, index);
        try {
            return value != null ? Long.parseLong(value) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 구글 스프레드 시트 API URL 생성 (시트 이름, API 키, 시트 ID 사용)
     */
    private URI getSheetUri(String sheetName) {
        return UriComponentsBuilder
                .fromUriString("https://sheets.googleapis.com/v4/spreadsheets/{sheetId}/values/{sheetName}")
                .queryParam("key", sheetKey)
                .buildAndExpand(sheetId, sheetName)
                .toUri();
    }
}

/**
 * 구글 스프레드 시트 API의 응답 JSON을 매핑하기 위한 클래스.
 */
@Getter
class GoogleSuccessSheetResponse {
    private List<List<Object>> values;

    public void setValues(List<List<Object>> values) {
        this.values = values;
    }
}
