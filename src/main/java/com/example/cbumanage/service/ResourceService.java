package com.example.cbumanage.service;

import com.example.cbumanage.dto.ResourceCreateRequestDTO;
import com.example.cbumanage.dto.ResourceListItemDTO;
import com.example.cbumanage.exception.MemberNotExistsException;
import com.example.cbumanage.model.CbuMember;
import com.example.cbumanage.model.Resource;
import com.example.cbumanage.repository.CbuMemberRepository;
import com.example.cbumanage.repository.ResourceRepository;
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
    private final CbuMemberRepository cbuMemberRepository;

    public ResourceService(ResourceRepository resourceRepository, CbuMemberRepository cbuMemberRepository) {
        this.resourceRepository = resourceRepository;
        this.cbuMemberRepository = cbuMemberRepository;
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

        Resource resource = Resource.builder()
                .member(member)
                .title(request.getTitle())
                .link(request.getLink())
                .build();

        Resource saved = resourceRepository.save(resource);
        return ResourceListItemDTO.from(saved);
    }

    /**
     * 자료방 게시글 목록을 페이지네이션하여 조회합니다.
     *
     * @param pageable 페이지네이션 정보
     * @return 페이지네이션된 게시글 목록 DTO
     */
    public Page<ResourceListItemDTO> getResources(Pageable pageable) {
        return resourceRepository.findAll(pageable)
                .map(ResourceListItemDTO::from);
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

        if (!resource.getMember().getCbuMemberId().equals(memberId)) {
            throw new com.example.cbumanage.exception.MemberDoesntHavePermissionException("이 자료를 삭제할 권한이 없습니다.");
        }

        resourceRepository.delete(resource);
    }
}
