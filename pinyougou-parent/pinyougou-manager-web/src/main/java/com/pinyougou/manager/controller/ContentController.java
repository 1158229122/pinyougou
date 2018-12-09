package com.pinyougou.manager.controller;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pinyougou.content.service.ContentCategoryService;
import com.pinyougou.content.service.ContentService;
import com.pinyougou.pojo.TbContentCategory;
import com.pinyougou.utils.FastDFSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbContent;


import entity.PageResult;
import entity.Result;
import org.springframework.web.multipart.MultipartFile;

/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/content")
public class ContentController {

	@Reference
	private ContentService contentService;
	@Reference
	private ContentCategoryService contentCategoryService;

	@Value("${FILE_SERVER_URL}")
	private String fileServerUrl;
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbContent> findAll(){			
		return contentService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return contentService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param content
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody TbContent content){
		try {
			contentService.add(content);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param content
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody TbContent content){
		try {
			contentService.update(content);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public TbContent findOne(Long id){
		return contentService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long [] ids){
		try {
			contentService.delete(ids);
			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param content
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbContent content, int page, int rows  ){
		return contentService.findPage(content, page, rows);		
	}
	@RequestMapping("/upload")
	public Result upload(MultipartFile file) {
		try {

			String originalFilename = file.getOriginalFilename();
			String extName = originalFilename.substring(originalFilename.lastIndexOf(".")+1);
			FastDFSClient fastDFSClient = new FastDFSClient();
			String url = fastDFSClient.uploadFile(file.getBytes(), extName);

			return new Result(true, fileServerUrl+url);
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "");
		}
	}
	@RequestMapping("selectOptionsList")
	public List<Map> selectOptionsList() {
		List<TbContentCategory> categories = contentCategoryService.findAll();
		List<Map> list = new ArrayList<Map>();
		for (TbContentCategory category : categories) {
			Map map = new HashMap();
			map.put("id",category.getId());
			map.put("text",category.getName());
			list.add(map);
		}
		return list;
	}
}
