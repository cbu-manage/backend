package com.example.cbumanage.service;

import com.example.cbumanage.dto.OgMetaPreviewDTO;
import com.example.cbumanage.dto.ResourceCreateRequestDTO;
import com.example.cbumanage.dto.ResourceListItemDTO;
import com.example.cbumanage.exception.MemberDoesntHavePermissionException;
import com.example.cbumanage.exception.MemberNotExistsException;
import com.example.cbumanage.model.CbuMember;
import com.example.cbumanage.model.Post;
import com.example.cbumanage.model.Resource;
import com.example.cbumanage.repository.CbuMemberRepository;
import com.example.cbumanage.repository.PostRepository;
import com.example.cbumanage.repository.ResourceRepository;
import com.example.cbumanage.utils.OgMetaParser;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 자료방 게시글 관련 비즈니스 로직을 처리하는 서비스입니다.
 */
@Service
@Transactional(readOnly = true)
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final PostRepository postRepository;
    private final CbuMemberRepository cbuMemberRepository;
    private final OgMetaParser ogMetaParser;

    public ResourceService(ResourceRepository resourceRepository, PostRepository postRepository,
                           CbuMemberRepository cbuMemberRepository, OgMetaParser ogMetaParser) {
        this.resourceRepository = resourceRepository;
        this.postRepository = postRepository;
        this.cbuMemberRepository = cbuMemberRepository;
        this.ogMetaParser = ogMetaParser;
    }

    /**
     * 자료방 게시글을 등록합니다.
     *
     * @param request  게시글 생성 요청 DTO (제목, 링크)
     * @param memberId 작성자 회원 ID
     * @return 생성된 게시글 정보 DTO
     */
    @Transactional
    public ResourceListItemDTO createResource(ResourceCreateRequestDTO request, Long memberId) {
        CbuMember member = cbuMemberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotExistsException("ID가 " + memberId + "인 회원을 찾을 수 없습니다."));

        Post post = Post.create(member.getCbuMemberId(), request.getTitle(), "", 6);
        Post savedPost = postRepository.save(post);

        Resource resource = Resource.builder()
                .post(savedPost)
                .link(request.getLink())
                .build();

        resource.updateOg(request.getOgImage(), request.getOgDescription());

        resourceRepository.save(resource);
        return ResourceListItemDTO.from(resource, member);
    }

    /**
     * 자료방 게시글 목록을 페이지네이션하여 조회합니다.
     *
     * @param pageable 페이지네이션 정보
     * @return 페이지네이션된 게시글 목록 DTO
     */
    public Page<ResourceListItemDTO> getResources(Pageable pageable) {
        return resourceRepository.findAll(pageable)
                .map(r -> {
                    CbuMember author = cbuMemberRepository.findById(r.getPost().getAuthorId())
                            .orElseThrow(() -> new MemberNotExistsException("작성자를 찾을 수 없습니다."));
                    return ResourceListItemDTO.from(r, author);
                });
    }

    /**
     * 내가 작성한 자료방 게시글 목록을 조회합니다.
     *
     * @param memberId 조회할 회원 ID
     * @param pageable 페이지네이션 정보
     * @return 페이지네이션된 내 게시글 목록 DTO
     */
    public Page<ResourceListItemDTO> getMyResources(Long memberId, Pageable pageable) {
        CbuMember member = cbuMemberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotExistsException("ID가 " + memberId + "인 회원을 찾을 수 없습니다."));
        return resourceRepository.findByPostAuthorId(memberId, pageable)
                .map(r -> ResourceListItemDTO.from(r, member));
    }

    /**
     * URL에서 OG 메타 정보를 파싱하여 반환합니다. (저장 없이 미리보기용)
     *
     * @param url 미리보기할 외부 URL
     * @return OG 제목, 이미지, 설명
     */
    public OgMetaPreviewDTO previewOg(String url) {
        return new OgMetaPreviewDTO(ogMetaParser.parse(url));
    }

    /**
     * 자료방 게시글의 OG 메타 정보를 URL에서 다시 파싱하여 갱신합니다.
     * 작성자 본인만 갱신할 수 있습니다.
     *
     * @param resourceId 갱신할 게시글 ID
     * @param memberId   요청 회원 ID
     */
    @Transactional
    public void refreshOg(Long resourceId, Long memberId) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new EntityNotFoundException("ID가 " + resourceId + "인 자료를 찾을 수 없습니다."));

        if (!resource.getPost().getAuthorId().equals(memberId)) {
            throw new MemberDoesntHavePermissionException("이 자료의 OG 정보를 갱신할 권한이 없습니다.");
        }

        OgMetaParser.OgMeta ogMeta = ogMetaParser.parse(resource.getLink());
        resource.updateOg(ogMeta.image(), ogMeta.description());
    }

    /**
     * 자료방 게시글을 삭제합니다. (소프트 딜리트)
     * 작성자 본인만 삭제할 수 있습니다.
     *
     * @param resourceId 삭제할 게시글 ID
     * @param memberId   삭제 요청 회원 ID
     */
    @Transactional
    public void deleteResource(Long resourceId, Long memberId) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new EntityNotFoundException("ID가 " + resourceId + "인 자료를 찾을 수 없습니다."));

        if (!resource.getPost().getAuthorId().equals(memberId)) {
            throw new MemberDoesntHavePermissionException("이 자료를 삭제할 권한이 없습니다.");
        }

        resource.getPost().delete();
        resourceRepository.delete(resource);
    }
}
