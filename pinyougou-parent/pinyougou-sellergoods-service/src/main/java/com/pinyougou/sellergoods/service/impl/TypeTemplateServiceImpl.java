package com.pinyougou.sellergoods.service.impl;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONAware;
import com.alibaba.fastjson.JSONObject;
import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbSpecificationOptionExample;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbTypeTemplateMapper;
import com.pinyougou.pojo.TbTypeTemplate;
import com.pinyougou.pojo.TbTypeTemplateExample;
import com.pinyougou.pojo.TbTypeTemplateExample.Criteria;
import com.pinyougou.sellergoods.service.TypeTemplateService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class TypeTemplateServiceImpl implements TypeTemplateService {

	@Autowired
	private TbTypeTemplateMapper typeTemplateMapper;
	@Autowired
	private RedisTemplate redisTemplate;
	/**
	 * 查询全部
	 */
	@Override
	public List<TbTypeTemplate> findAll() {
		return typeTemplateMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbTypeTemplate> page=   (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbTypeTemplate typeTemplate) {
		typeTemplateMapper.insert(typeTemplate);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbTypeTemplate typeTemplate){

		typeTemplateMapper.updateByPrimaryKey(typeTemplate);
	}	


	@Autowired
	private TbSpecificationOptionMapper tbSpecificationOptionMapper;
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbTypeTemplate findOne(Long id){

		TbTypeTemplate typeTemplate = typeTemplateMapper.selectByPrimaryKey(id);
		//[{"id":34,"text":"尺寸"},{"id":35,"text":"颜色"}]
		String specIds = typeTemplate.getSpecIds();
		List<Map> specMaps = JSONObject.parseArray(specIds, Map.class);
		for (Map specMap : specMaps) {

			Integer sepcId = (Integer) specMap.get("id");
			//根据规格id获取对应的规格选项
			TbSpecificationOptionExample specificationOptionExample = new TbSpecificationOptionExample();
			specificationOptionExample.createCriteria().andSpecIdEqualTo(Long.valueOf(sepcId));
			List<TbSpecificationOption> specificationOptionList = tbSpecificationOptionMapper.selectByExample(specificationOptionExample);
			//把对应的规格选项拼接到每一个spec
			specMap.put("options", specificationOptionList);
		}
		//[{"id":34,"text":"尺寸","options":[]},{"id":35,"text":"颜色"},"options":[]]
		//封装到
		typeTemplate.setSpecIds(JSON.toJSONString(specMaps));
		return typeTemplate;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {

		for(Long id:ids){
			typeTemplateMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbTypeTemplate typeTemplate, int pageNum, int pageSize) {
			saveToRedis();
		PageHelper.startPage(pageNum, pageSize);
		
		TbTypeTemplateExample example=new TbTypeTemplateExample();
		Criteria criteria = example.createCriteria();
		
		if(typeTemplate!=null){			
						if(typeTemplate.getName()!=null && typeTemplate.getName().length()>0){
				criteria.andNameLike("%"+typeTemplate.getName()+"%");
			}
			if(typeTemplate.getSpecIds()!=null && typeTemplate.getSpecIds().length()>0){
				criteria.andSpecIdsLike("%"+typeTemplate.getSpecIds()+"%");
			}
			if(typeTemplate.getBrandIds()!=null && typeTemplate.getBrandIds().length()>0){
				criteria.andBrandIdsLike("%"+typeTemplate.getBrandIds()+"%");
			}
			if(typeTemplate.getCustomAttributeItems()!=null && typeTemplate.getCustomAttributeItems().length()>0){
				criteria.andCustomAttributeItemsLike("%"+typeTemplate.getCustomAttributeItems()+"%");
			}
	
		}
		
		Page<TbTypeTemplate> page= (Page<TbTypeTemplate>)typeTemplateMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	private void saveToRedis(){
		List<TbTypeTemplate> typeTemplateList = findAll();
		//缓存品牌
		for (TbTypeTemplate typeTemplate : typeTemplateList) {
			String brandIds = typeTemplate.getBrandIds();
			List<Map> brandList = JSON.parseArray(brandIds, Map.class);
			//保存品牌
			redisTemplate.boundHashOps("brandList").put(typeTemplate.getId(),brandList );
			//保存规格列表
            //存储规格列表
            TbTypeTemplate one = findOne(typeTemplate.getId());
            String specIds = one.getSpecIds();
            List<Map> specList = JSON.parseArray(specIds, Map.class);
			redisTemplate.boundHashOps("specList").put(typeTemplate.getId(), specList);
			System.out.println("保存了品牌");
		}


	}
}
