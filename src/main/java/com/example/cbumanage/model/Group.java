package com.example.cbumanage.model;

import com.example.cbumanage.model.enums.GroupMemberRole;
import com.example.cbumanage.model.enums.GroupMemberStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="groups")
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_name",unique = true, nullable = false)
    private String groupName;

    @CreatedDate
    public LocalDateTime createdAt;

    @LastModifiedDate
    public LocalDateTime updatedAt;

    public Group(String groupName) {
        this.groupName = groupName;
    }

    public static Group create(String groupName,CbuMember cbuMember) {
        Group  group = new Group(groupName);
        GroupMember leader = GroupMember.create(group,cbuMember,GroupMemberStatus.ACTIVE,GroupMemberRole.LEADER);
        group.members.add(leader);
        return group;
    }

    public void changeGroupName(String groupName) {
        this.groupName = groupName;
    }

    @OneToMany(mappedBy = "group" ,
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<GroupMember> members = new ArrayList<>();

    public void addMember(CbuMember cbuMember) {
       this.members.add(GroupMember.create(this,cbuMember,GroupMemberStatus.PENDING,GroupMemberRole.MEMBER));
    }

}
