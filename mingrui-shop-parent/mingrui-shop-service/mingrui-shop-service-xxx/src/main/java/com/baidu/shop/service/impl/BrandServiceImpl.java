package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.BrandDTO;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.entity.CategoryBrandEntity;
import com.baidu.shop.mapper.BrandMapper;
import com.baidu.shop.mapper.CategoryBrandMapper;
import com.baidu.shop.service.BrandService;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.PinyinUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName BrandServiceImpl
 * @Description: TODO
 * @Author wangjing
 * @Date 2021/01/22
 * @Version V1.0
 **/

@RestController
public class BrandServiceImpl extends BaseApiService implements BrandService {

    private BrandMapper brandMapper;

    @Autowired
    private CategoryBrandMapper categoryBrandMapper;

    @Autowired
    public void setBrandMapper(BrandMapper brandMapper){
        this.brandMapper = brandMapper;
    }

    @Override
    public Result<PageInfo<BrandEntity>> getBrandInfo(BrandDTO brandDTO) {
        PageHelper.startPage(brandDTO.getPage(),brandDTO.getRows());

        if (!StringUtils.isEmpty(brandDTO.getSort())) PageHelper.orderBy(brandDTO.getOrderBy());

        BrandEntity brandEntity = BaiduBeanUtil.copyProperties(brandDTO,BrandEntity.class);

        Example example = new Example(BrandEntity.class);
        if (!StringUtils.isEmpty(brandEntity.getName()))
            example.createCriteria().andLike("name","%" + brandEntity.getName() + "%");

        List<BrandEntity> brandEntities = brandMapper.selectByExample(example);
        PageInfo<BrandEntity> pageInfo = new PageInfo<>(brandEntities);
        return this.setResultSuccess(pageInfo);
    }

    @Transactional
    @Override
    public Result<JSONObject> saveBrandInfo(BrandDTO brandDTO) {
        BrandEntity brandEntity = BaiduBeanUtil.copyProperties(brandDTO, BrandEntity.class);
        //处理品牌首字母
        brandEntity.setLetter(PinyinUtil.getUpperCase(String.valueOf(brandEntity.getName().toCharArray()[0]),false).toCharArray()[0]);
        brandMapper.insertSelective(brandEntity);

        //维护中间表数据
        this.insertCategoryBrandList(brandDTO.getCategories(),brandEntity.getId());
        return this.setResultSuccess();
    }

    @Transactional
    @Override
    public Result<JSONObject> editBrandInfo(BrandDTO brandDTO) {
        BrandEntity brandEntity = BaiduBeanUtil.copyProperties(brandDTO,BrandEntity.class);
        brandEntity.setLetter(PinyinUtil.getUpperCase(String.valueOf(brandEntity.getName().toCharArray()[0]),false).toCharArray()[0]);
        brandMapper.updateByPrimaryKeySelective(brandEntity);

        this.deleteCategoryBrandByBrandId(brandEntity.getId());
        this.insertCategoryBrandList(brandDTO.getCategories(),brandEntity.getId());
        return this.setResultSuccess();
    }

    @Transactional
    @Override
    public Result<JSONObject> deleteBrandInfo(Integer id) {
        brandMapper.deleteByPrimaryKey(id);

        this.deleteCategoryBrandByBrandId(id);
        return this.setResultSuccess();
    }

    @Override
    public Result<List<BrandEntity>> getBrandInfoByCategoryId(Integer cid) {
        List<BrandEntity> list = brandMapper.getBrandInfoByCategoryId(cid);
        return this.setResultSuccess(list);
    }

    private void deleteCategoryBrandByBrandId(Integer brandId) {
        Example example = new Example(CategoryBrandEntity.class);
        example.createCriteria().andEqualTo("brandId",brandId);
        categoryBrandMapper.deleteByExample(example);
    }

    private void insertCategoryBrandList(String categories, Integer brandId){
        // 自定义异常
        if(StringUtils.isEmpty(categories)) throw new RuntimeException("分类信息不能为空");

        //判断分类集合字符串中是否包含,
        if(categories.contains(",")){//多个分类 --> 批量新增

            categoryBrandMapper.insertList(
                    Arrays.asList(categories.split(","))
                            .stream()
                            .map(categoryIdStr -> new CategoryBrandEntity(Integer.valueOf(categoryIdStr)
                                    ,brandId))
                            .collect(Collectors.toList())
            );

        }else{//普通单个新增

            CategoryBrandEntity categoryBrandEntity = new CategoryBrandEntity();
            categoryBrandEntity.setBrandId(brandId);
            categoryBrandEntity.setCategoryId(Integer.valueOf(categories));

            categoryBrandMapper.insertSelective(categoryBrandEntity);
        }
    }

}
