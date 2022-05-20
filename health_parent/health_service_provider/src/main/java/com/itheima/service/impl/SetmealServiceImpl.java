package com.itheima.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itheima.constant.RedisConstant;
import com.itheima.dao.SetmealDao;
import com.itheima.entity.PageResult;
import com.itheima.entity.QueryPageBean;
import com.itheima.pojo.Setmeal;
import com.itheima.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.JedisPool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LiuPei
 * @date 2022/5/17 2:44
 * 体检套餐服务
 */

@Service(interfaceClass = SetmealService.class)
@Transactional
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealDao setmealDao;

    //使用JedisPool操作Redis服务
    @Autowired
    private JedisPool jedisPool;

    //新增套餐信息，同时需要关联检查组
    @Override
    public void add(Setmeal setmeal, Integer[] checkgroupIds) {
        setmealDao.add(setmeal);
        Integer setmealId = setmeal.getId();
        this.setSetmealAndCheckGroup(setmealId, checkgroupIds);

        //新增套餐成功后，将图片名称取出来
        String imgFile = setmeal.getImg();
        //上传图面了就保存到Redis
        if (imgFile != null && imgFile.length() > 0) {
            //新增套餐成功后，将图片名称保存到Redis
            jedisPool.getResource().sadd(RedisConstant.SETMEAL_PIC_DB_RESOURCES, imgFile);
        }
    }

    /**
     * 分页查询
     *
     * @param queryPageBean
     * @return
     */
    @Override
    public PageResult pageQuery(QueryPageBean queryPageBean) {
        Integer currentPage = queryPageBean.getCurrentPage();
        Integer pageSize = queryPageBean.getPageSize();
        String queryString = queryPageBean.getQueryString();
        PageHelper.startPage(currentPage, pageSize);
        Page<Setmeal> page = setmealDao.findByCondition(queryString);
        return new PageResult(page.getTotal(), page.getResult());
    }

    //  设置套餐和检查组多对多关系，操作t_setmeal_checkgroup
    public void setSetmealAndCheckGroup(Integer setmealId, Integer[] checkgroupIds) {
        if (checkgroupIds != null && checkgroupIds.length > 0) {
            for (Integer checkgroupId : checkgroupIds) {
                Map<String, Integer> map = new HashMap<>();
                map.put("setmeal_id", setmealId);
                map.put("checkgroup_id", checkgroupId);
                setmealDao.setSetmealAndCheckGroup(map);
            }
        }
    }

    /**
     * 根据id删除套餐
     *
     * @param id
     */
    @Override
    public void deleteById(Integer id) {
        //查询当前检查组的照片
        Setmeal setmeal = this.findById(id);
        String imgFile = setmeal.getImg();

        //判断当前套餐是否已经关联到检查组
        long count = setmealDao.findCountByCheckGroupId(id);
        if (count > 0) {
            //当前套餐已经关联到检查组，需要删除套餐关联的检查组
            setmealDao.deleteAssoication(id);
        }
        //当前套餐没有关联到检查组，直接删除套餐基本信息
        setmealDao.deleteById(id);

        //如果当前套餐有图片就删除，如果没有图片就不用删除
        if (imgFile != null && imgFile.length() > 0) {
            //等待当前套餐删除完成后，再从Redis成功集合中删除图片名称
            jedisPool.getResource().srem(RedisConstant.SETMEAL_PIC_DB_RESOURCES, imgFile);
        }
    }

    /**
     * 编辑套餐，同时需要让套餐关联检查组
     *
     * @param setmeal
     * @param checkGroupIds
     */
    @Override
    public void edit(Setmeal setmeal, Integer[] checkGroupIds) {
        //获取当前照片名称
        Setmeal setmeal1 = this.findById(setmeal.getId());
        String imgFile1 = setmeal1.getImg();

        //修改套餐基本信息，操作t_checkgroup表
        setmealDao.edit(setmeal);
        //清理当前套餐关联的检查组，操作中间关系表t_setmeal_checkgroup表
        setmealDao.deleteAssoication(setmeal.getId());
        //重新建立当前检查组和检查项的关联关系
        Integer setmealId = setmeal.getId();
        this.setSetmealAndCheckGroup(setmealId, checkGroupIds);

        //如果之前的图片不为空，就在更新图片之后删除他
        if (imgFile1 != null && imgFile1.length() > 0) {
            //等待修改信息成功后， 从Redis成功集合中删除修改前图片名称
            jedisPool.getResource().srem(RedisConstant.SETMEAL_PIC_DB_RESOURCES, imgFile1);
        }
        //修改套餐成功后，将修改图片名称取出来
        String imgFile2 = setmeal.getImg();
        //修改图片了就保存到Redis
        if (imgFile2 != null && imgFile2.length() > 0) {
            //修改套餐成功后，将图片名称保存到Redis
            jedisPool.getResource().sadd(RedisConstant.SETMEAL_PIC_DB_RESOURCES, imgFile2);
        }

    }

    /**
     * 根据id查询套餐
     *
     * @param id
     * @return
     */
    @Override
    public Setmeal findById(Integer id) {
        return setmealDao.findById(id);
    }

    /**
     * 根据套餐ID查询关联的检查组ID
     *
     * @param id
     * @return
     */
    @Override
    public List<Integer> findCheckGroupIdsBySetmealId(Integer id) {
        return setmealDao.findCheckGroupIdsBySetmealId(id);
    }

}

