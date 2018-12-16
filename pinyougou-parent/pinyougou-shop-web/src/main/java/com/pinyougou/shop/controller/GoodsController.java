package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.utils.FastDFSClient;
import entity.Goods;
import entity.PageResult;
import entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;

	@Value("${FILE_SERVER_URL}")
	private String fileServerUrl;

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


	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody Goods goods){
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String sellerId = authentication.getName();
			goods.getGoods().setSellerId(sellerId);
			//添加到索引库
			goodsService.add(goods);


			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return goodsService.findPage(page, rows);
	}

	@RequestMapping("updateStatus")
	public Result updateStatus(Long ids[], String status) {
		Result result = new Result();
		try {
			goodsService.updateStatus(ids, status);
			result.setSuccess(true);
			result.setMessage("提交成功");
		} catch (Exception e) {
			e.printStackTrace();
			result.setMessage("提交失败");
		}
		return result;
	}


	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
		try {
			//需要对修改的商品进行校验
			Long id = goods.getGoods().getId();
			Goods dbGoods = goodsService.findOne(id);
			//如果查询的商品不是自己的不能修改
			String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
			if (!sellerId.equals(dbGoods.getGoods().getSellerId())) {
				throw new RuntimeException("当前修改的不是您自己的商品");
			}

			//修改商品的时候不能把自己的商品给别的商家了
			if (!sellerId.equals(goods.getGoods().getSellerId())) {
				throw new RuntimeException("不能修改商品的sellerId");
			}

			goodsService.update(goods);
			System.out.println(JSON.toJSONString(goods));
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, e.getMessage());
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){

		return goodsService.findOne(id);
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long [] ids){
		try {
			goodsService.delete(ids);
			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param goods
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){

		String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
		goods.setSellerId(sellerId);
		return goodsService.findPage(goods, page, rows);		
	}
	
}
