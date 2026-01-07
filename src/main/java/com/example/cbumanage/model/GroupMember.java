package com.example.cbumanage.model;


import com.example.cbumanage.model.enums.GroupMemberRole;
import com.example.cbumanage.model.enums.GroupMemberStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Table(name = "group_member")
@Getter
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="group_id",nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cbumember_id",nullable = false)
    private CbuMember cbuMember;

    @Enumerated(EnumType.STRING)
    private GroupMemberStatus groupMemberStatus;

    @Enumerated(EnumType.STRING)
    private GroupMemberRole groupMemberRole;

    public GroupMember(Group group,
                       CbuMember cbuMember,
                       GroupMemberStatus groupMemberStatus,
                       GroupMemberRole groupMemberRole) {
        this.group = group;
        this.cbuMember = cbuMember;
        this.groupMemberStatus = groupMemberStatus;
        this.groupMemberRole = groupMemberRole;
    }

    public static GroupMember create(Group group, CbuMember cbuMember, GroupMemberStatus groupMemberStatus, GroupMemberRole groupMemberRole) {
        return  new GroupMember(group, cbuMember, groupMemberStatus, groupMemberRole);
    }

    public void changeStatus(GroupMemberStatus groupMemberStatus) {
        this.groupMemberStatus = groupMemberStatus;
    }

    public void changeRole(GroupMemberRole groupMemberRole) {
        this.groupMemberRole = groupMemberRole;
    }

}
