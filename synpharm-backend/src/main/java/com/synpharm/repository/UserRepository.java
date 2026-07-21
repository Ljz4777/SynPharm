package com.synpharm.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.synpharm.model.entity.SysUser;
import com.synpharm.repository.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 用户仓储
 */
@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final SysUserMapper sysUserMapper;

    public SysUser findByEmail(String email) {
        LambdaQueryWrapper<SysUser> query = Wrappers.lambdaQuery();
        query.eq(SysUser::getEmail, email);
        query.eq(SysUser::getDeleted, 0);
        return sysUserMapper.selectOne(query);
    }

    public SysUser findById(Long id) {
        return sysUserMapper.selectById(id);
    }

    public void save(SysUser user) {
        if (user.getId() == null) {
            sysUserMapper.insert(user);
        } else {
            sysUserMapper.updateById(user);
        }
    }

    public void deleteById(Long id) {
        sysUserMapper.deleteById(id);
    }
}