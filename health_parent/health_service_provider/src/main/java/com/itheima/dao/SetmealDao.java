package com.itheima.dao;

import com.github.pagehelper.Page;
import com.itheima.pojo.CheckGroup;
import com.itheima.pojo.Setmeal;

import java.util.List;
import java.util.Map;

/**
 * @author LiuPei
 * @date 2022/5/17 2:46
 */

public interface SetmealDao {

    public void add(Setmeal setmeal);

    public void setSetmealAndCheckGroup(Map map);

    public Page<Setmeal> findByCondition(String queryString);

    public void deleteById(Integer id);

    public long findCountByCheckGroupId(Integer id);

    public void deleteAssoication(Integer id);

    public Setmeal findById(Integer id);

    public void edit(Setmeal setmeal);

    public List<Integer> findCheckGroupIdsBySetmealId(Integer id);
}
