
package com.controller;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import java.util.*;
import org.springframework.beans.BeanUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import com.service.TokenService;
import com.utils.*;
import java.lang.reflect.InvocationTargetException;

import com.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;
import com.annotation.IgnoreAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.entity.*;
import com.entity.view.*;
import com.service.*;
import com.utils.PageUtils;
import com.utils.R;
import com.alibaba.fastjson.*;

/**
 * 活动报名
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/huodongOrder")
public class HuodongOrderController {
    private static final Logger logger = LoggerFactory.getLogger(HuodongOrderController.class);

    @Autowired
    private HuodongOrderService huodongOrderService;


    @Autowired
    private TokenService tokenService;
    @Autowired
    private DictionaryService dictionaryService;

    //级联表service
    @Autowired
    private HuodongService huodongService;
    @Autowired
    private YonghuService yonghuService;



    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永不会进入");
        else if("用户".equals(role))
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        else if("工作人员".equals(role))
            params.put("gongzuorenyuanId",request.getSession().getAttribute("userId"));
        if(params.get("orderBy")==null || params.get("orderBy")==""){
            params.put("orderBy","id");
        }
        PageUtils page = huodongOrderService.queryPage(params);

        //字典表数据转换
        List<HuodongOrderView> list =(List<HuodongOrderView>)page.getList();
        for(HuodongOrderView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c, request);
        }
        return R.ok().put("data", page);
    }

    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        HuodongOrderEntity huodongOrder = huodongOrderService.selectById(id);
        if(huodongOrder !=null){
            //entity转view
            HuodongOrderView view = new HuodongOrderView();
            BeanUtils.copyProperties( huodongOrder , view );//把实体数据重构到view中

                //级联表
                HuodongEntity huodong = huodongService.selectById(huodongOrder.getHuodongId());
                if(huodong != null){
                    BeanUtils.copyProperties( huodong , view ,new String[]{ "id", "createTime", "insertTime", "updateTime"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setHuodongId(huodong.getId());
                }
                //级联表
                YonghuEntity yonghu = yonghuService.selectById(huodongOrder.getYonghuId());
                if(yonghu != null){
                    BeanUtils.copyProperties( yonghu , view ,new String[]{ "id", "createTime", "insertTime", "updateTime"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setYonghuId(yonghu.getId());
                }
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view, request);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody HuodongOrderEntity huodongOrder, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,huodongOrder:{}",this.getClass().getName(),huodongOrder.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永远不会进入");
        else if("用户".equals(role))
            huodongOrder.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));

        huodongOrder.setInsertTime(new Date());
        huodongOrder.setCreateTime(new Date());
        huodongOrderService.insert(huodongOrder);
        return R.ok();
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody HuodongOrderEntity huodongOrder, HttpServletRequest request){
        logger.debug("update方法:,,Controller:{},,huodongOrder:{}",this.getClass().getName(),huodongOrder.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
//        if(false)
//            return R.error(511,"永远不会进入");
//        else if("用户".equals(role))
//            huodongOrder.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));
        //根据字段查询是否有相同数据
        Wrapper<HuodongOrderEntity> queryWrapper = new EntityWrapper<HuodongOrderEntity>()
            .eq("id",0)
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        HuodongOrderEntity huodongOrderEntity = huodongOrderService.selectOne(queryWrapper);
        if("".equals(huodongOrder.getHuodongOrderJiankangmaPhoto()) || "null".equals(huodongOrder.getHuodongOrderJiankangmaPhoto())){
                huodongOrder.setHuodongOrderJiankangmaPhoto(null);
        }
        if("".equals(huodongOrder.getHuodongOrderXingchengmaPhoto()) || "null".equals(huodongOrder.getHuodongOrderXingchengmaPhoto())){
                huodongOrder.setHuodongOrderXingchengmaPhoto(null);
        }
        if("".equals(huodongOrder.getHuodongOrderHesuanbaogaoPhoto()) || "null".equals(huodongOrder.getHuodongOrderHesuanbaogaoPhoto())){
                huodongOrder.setHuodongOrderHesuanbaogaoPhoto(null);
        }
        if(huodongOrderEntity==null){
            huodongOrderService.updateById(huodongOrder);//根据id更新
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }


    /**
    * 审核
    */
    @RequestMapping("/shenhe")
    public R shenhe(@RequestBody HuodongOrderEntity huodongOrder, HttpServletRequest request){
        logger.debug("shenhe方法:,,Controller:{},,huodongOrder:{}",this.getClass().getName(),huodongOrder.toString());

        if(huodongOrder.getHuodongOrderYesnoTypes() == 2){//通过
            huodongOrder.setHuodongOrderTypes(2);
        }else if(huodongOrder.getHuodongOrderYesnoTypes() == 3){//拒绝
            huodongOrder.setHuodongOrderTypes(3);
        }
        huodongOrder.setHuodongOrderShenheTime(new Date());//审核时间
        huodongOrderService.updateById(huodongOrder);//审核
        return R.ok();
    }

    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        huodongOrderService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }


    /**
     * 批量上传
     */
    @RequestMapping("/batchInsert")
    public R save( String fileName, HttpServletRequest request){
        logger.debug("batchInsert方法:,,Controller:{},,fileName:{}",this.getClass().getName(),fileName);
        Integer yonghuId = Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId")));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            List<HuodongOrderEntity> huodongOrderList = new ArrayList<>();//上传的东西
            Map<String, List<String>> seachFields= new HashMap<>();//要查询的字段
            Date date = new Date();
            int lastIndexOf = fileName.lastIndexOf(".");
            if(lastIndexOf == -1){
                return R.error(511,"该文件没有后缀");
            }else{
                String suffix = fileName.substring(lastIndexOf);
                if(!".xls".equals(suffix)){
                    return R.error(511,"只支持后缀为xls的excel文件");
                }else{
                    URL resource = this.getClass().getClassLoader().getResource("../../upload/" + fileName);//获取文件路径
                    File file = new File(resource.getFile());
                    if(!file.exists()){
                        return R.error(511,"找不到上传文件，请联系管理员");
                    }else{
                        List<List<String>> dataList = PoiUtil.poiImport(file.getPath());//读取xls文件
                        dataList.remove(0);//删除第一行，因为第一行是提示
                        for(List<String> data:dataList){
                            //循环
                            HuodongOrderEntity huodongOrderEntity = new HuodongOrderEntity();
//                            huodongOrderEntity.setHuodongOrderUuidNumber(data.get(0));                    //报名编号 要改的
//                            huodongOrderEntity.setHuodongId(Integer.valueOf(data.get(0)));   //活动 要改的
//                            huodongOrderEntity.setYonghuId(Integer.valueOf(data.get(0)));   //用户 要改的
//                            huodongOrderEntity.setHuodongOrderJiankangmaPhoto("");//详情和图片
//                            huodongOrderEntity.setHuodongOrderXingchengmaPhoto("");//详情和图片
//                            huodongOrderEntity.setHuodongOrderHesuanbaogaoPhoto("");//详情和图片
//                            huodongOrderEntity.setHuodongOrderJieshaoText(data.get(0));                    //个人介绍 要改的
//                            huodongOrderEntity.setHuodongOrderTypes(Integer.valueOf(data.get(0)));   //报名状态 要改的
//                            huodongOrderEntity.setHuodongOrderYesnoTypes(Integer.valueOf(data.get(0)));   //审核状态 要改的
//                            huodongOrderEntity.setHuodongOrderYesnoText(data.get(0));                    //审核意见 要改的
//                            huodongOrderEntity.setHuodongOrderShenheTime(sdf.parse(data.get(0)));          //审核时间 要改的
//                            huodongOrderEntity.setInsertTime(date);//时间
//                            huodongOrderEntity.setCreateTime(date);//时间
                            huodongOrderList.add(huodongOrderEntity);


                            //把要查询是否重复的字段放入map中
                                //报名编号
                                if(seachFields.containsKey("huodongOrderUuidNumber")){
                                    List<String> huodongOrderUuidNumber = seachFields.get("huodongOrderUuidNumber");
                                    huodongOrderUuidNumber.add(data.get(0));//要改的
                                }else{
                                    List<String> huodongOrderUuidNumber = new ArrayList<>();
                                    huodongOrderUuidNumber.add(data.get(0));//要改的
                                    seachFields.put("huodongOrderUuidNumber",huodongOrderUuidNumber);
                                }
                        }

                        //查询是否重复
                         //报名编号
                        List<HuodongOrderEntity> huodongOrderEntities_huodongOrderUuidNumber = huodongOrderService.selectList(new EntityWrapper<HuodongOrderEntity>().in("huodong_order_uuid_number", seachFields.get("huodongOrderUuidNumber")));
                        if(huodongOrderEntities_huodongOrderUuidNumber.size() >0 ){
                            ArrayList<String> repeatFields = new ArrayList<>();
                            for(HuodongOrderEntity s:huodongOrderEntities_huodongOrderUuidNumber){
                                repeatFields.add(s.getHuodongOrderUuidNumber());
                            }
                            return R.error(511,"数据库的该表中的 [报名编号] 字段已经存在 存在数据为:"+repeatFields.toString());
                        }
                        huodongOrderService.insertBatch(huodongOrderList);
                        return R.ok();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return R.error(511,"批量插入数据异常，请联系管理员");
        }
    }





    /**
    * 前端列表
    */
    @IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("list方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));

        // 没有指定排序字段就默认id倒序
        if(StringUtil.isEmpty(String.valueOf(params.get("orderBy")))){
            params.put("orderBy","id");
        }
        PageUtils page = huodongOrderService.queryPage(params);

        //字典表数据转换
        List<HuodongOrderView> list =(List<HuodongOrderView>)page.getList();
        for(HuodongOrderView c:list)
            dictionaryService.dictionaryConvert(c, request); //修改对应字典表字段
        return R.ok().put("data", page);
    }

    /**
    * 前端详情
    */
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("detail方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        HuodongOrderEntity huodongOrder = huodongOrderService.selectById(id);
            if(huodongOrder !=null){


                //entity转view
                HuodongOrderView view = new HuodongOrderView();
                BeanUtils.copyProperties( huodongOrder , view );//把实体数据重构到view中

                //级联表
                    HuodongEntity huodong = huodongService.selectById(huodongOrder.getHuodongId());
                if(huodong != null){
                    BeanUtils.copyProperties( huodong , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setHuodongId(huodong.getId());
                }
                //级联表
                    YonghuEntity yonghu = yonghuService.selectById(huodongOrder.getYonghuId());
                if(yonghu != null){
                    BeanUtils.copyProperties( yonghu , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setYonghuId(yonghu.getId());
                }
                //修改对应字典表字段
                dictionaryService.dictionaryConvert(view, request);
                return R.ok().put("data", view);
            }else {
                return R.error(511,"查不到数据");
            }
    }


    /**
    * 前端保存
    */
    @RequestMapping("/add")
    public R add(@RequestBody HuodongOrderEntity huodongOrder, HttpServletRequest request){
        logger.debug("add方法:,,Controller:{},,huodongOrder:{}",this.getClass().getName(),huodongOrder.toString());
            HuodongEntity huodongEntity = huodongService.selectById(huodongOrder.getHuodongId());
            if(huodongEntity == null)
                return R.error(511,"查不到该活动");


        HuodongOrderEntity huodongOrderEntity = huodongOrderService.selectOne(new EntityWrapper<HuodongOrderEntity>()
                .eq("yonghu_id", request.getSession().getAttribute("userId"))
                .eq("huodong_id", huodongEntity.getId())
        );
        if(huodongOrderEntity != null)
            return R.error("当前用户已经报名参与过当前活动");


        //计算所获得积分
            Integer userId = (Integer) request.getSession().getAttribute("userId");
            huodongOrder.setYonghuId(userId); //设置订单支付人id
            huodongOrder.setHuodongOrderUuidNumber(String.valueOf(new Date().getTime()));
            huodongOrder.setHuodongOrderTypes(1);

            huodongOrder.setInsertTime(new Date());
            huodongOrder.setCreateTime(new Date());
                huodongOrderService.insert(huodongOrder);//新增订单
            return R.ok();
    }


















}
