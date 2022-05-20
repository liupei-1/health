package com.itheima.service;

import com.itheima.entity.PageResult;
import com.itheima.entity.QueryPageBean;
import com.itheima.pojo.Setmeal;

import java.util.List;

/**
 * @author LiuPei
 * @date 2022/5/17 2:39
 */
public interface SetmealService {
    public void add(Setmeal setmeal , Integer[] checkgroupIds);

    public PageResult pageQuery(QueryPageBean queryPageBean);

    public void deleteById(Integer id);

    public void edit(Setmeal setmeal, Integer[] checkGroupIds);

    public Setmeal findById(Integer id);

    public List<Integer> findCheckGroupIdsBySetmealId(Integer id);
}
