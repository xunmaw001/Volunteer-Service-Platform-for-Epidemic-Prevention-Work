
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
 * 活动
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/huodong")
public class HuodongController {
    private static final Logger logger = LoggerFactory.getLogger(HuodongController.class);

    @Autowired
    private HuodongService huodongService;


    @Autowired
    private TokenService tokenService;
    @Autowired
    private DictionaryService dictionaryService;

    //级联表service

    @Autowired
    private YonghuService yonghuService;
    @Autowired
    private GongzuorenyuanService gongzuorenyuanService;


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
        params.put("huodongDeleteStart",1);params.put("huodongDeleteEnd",1);
        if(params.get("orderBy")==null || params.get("orderBy")==""){
            params.put("orderBy","id");
        }
        PageUtils page = huodongService.queryPage(params);

        //字典表数据转换
        List<HuodongView> list =(List<HuodongView>)page.getList();
        for(HuodongView c:list){
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
        HuodongEntity huodong = huodongService.selectById(id);
        if(huodong !=null){
            //entity转view
            HuodongView view = new HuodongView();
            BeanUtils.copyProperties( huodong , view );//把实体数据重构到view中

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
    public R save(@RequestBody HuodongEntity huodong, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,huodong:{}",this.getClass().getName(),huodong.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永远不会进入");

        Wrapper<HuodongEntity> queryWrapper = new EntityWrapper<HuodongEntity>()
            .eq("huodong_uuid_number", huodong.getHuodongUuidNumber())
            .eq("huodong_name", huodong.getHuodongName())
            .eq("huodong_address", huodong.getHuodongAddress())
            .eq("huodong_yiqing", huodong.getHuodongYiqing())
            .eq("huodong_types", huodong.getHuodongTypes())
            .eq("huodong_clicknum", huodong.getHuodongClicknum())
            .eq("shangxia_types", huodong.getShangxiaTypes())
            .eq("huodong_delete", huodong.getHuodongDelete())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        HuodongEntity huodongEntity = huodongService.selectOne(queryWrapper);
        if(huodongEntity==null){
            huodong.setHuodongClicknum(1);
            huodong.setShangxiaTypes(1);
            huodong.setHuodongDelete(1);
            huodong.setCreateTime(new Date());
            huodongService.insert(huodong);
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody HuodongEntity huodong, HttpServletRequest request){
        logger.debug("update方法:,,Controller:{},,huodong:{}",this.getClass().getName(),huodong.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
//        if(false)
//            return R.error(511,"永远不会进入");
        //根据字段查询是否有相同数据
        Wrapper<HuodongEntity> queryWrapper = new EntityWrapper<HuodongEntity>()
            .notIn("id",huodong.getId())
            .andNew()
            .eq("huodong_uuid_number", huodong.getHuodongUuidNumber())
            .eq("huodong_name", huodong.getHuodongName())
            .eq("huodong_address", huodong.getHuodongAddress())
            .eq("huodong_yiqing", huodong.getHuodongYiqing())
            .eq("huodong_types", huodong.getHuodongTypes())
            .eq("huodong_clicknum", huodong.getHuodongClicknum())
            .eq("shangxia_types", huodong.getShangxiaTypes())
            .eq("huodong_delete", huodong.getHuodongDelete())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        HuodongEntity huodongEntity = huodongService.selectOne(queryWrapper);
        if("".equals(huodong.getHuodongPhoto()) || "null".equals(huodong.getHuodongPhoto())){
                huodong.setHuodongPhoto(null);
        }
        if(huodongEntity==null){
            huodongService.updateById(huodong);//根据id更新
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }



    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        ArrayList<HuodongEntity> list = new ArrayList<>();
        for(Integer id:ids){
            HuodongEntity huodongEntity = new HuodongEntity();
            huodongEntity.setId(id);
            huodongEntity.setHuodongDelete(2);
            list.add(huodongEntity);
        }
        if(list != null && list.size() >0){
            huodongService.updateBatchById(list);
        }
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
            List<HuodongEntity> huodongList = new ArrayList<>();//上传的东西
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
                            HuodongEntity huodongEntity = new HuodongEntity();
//                            huodongEntity.setHuodongUuidNumber(data.get(0));                    //活动编号 要改的
//                            huodongEntity.setHuodongName(data.get(0));                    //活动名称 要改的
//                            huodongEntity.setHuodongAddress(data.get(0));                    //活动地点 要改的
//                            huodongEntity.setHuodongYiqing(data.get(0));                    //疫情信息 要改的
//                            huodongEntity.setHuodongPhoto("");//详情和图片
//                            huodongEntity.setHuodongTypes(Integer.valueOf(data.get(0)));   //活动类型 要改的
//                            huodongEntity.setKaishiTime(sdf.parse(data.get(0)));          //活动开始时间 要改的
//                            huodongEntity.setJieshuTime(sdf.parse(data.get(0)));          //活动结束时间 要改的
//                            huodongEntity.setHuodongClicknum(Integer.valueOf(data.get(0)));   //热度 要改的
//                            huodongEntity.setHuodongContent("");//详情和图片
//                            huodongEntity.setShangxiaTypes(Integer.valueOf(data.get(0)));   //是否上架 要改的
//                            huodongEntity.setHuodongDelete(1);//逻辑删除字段
//                            huodongEntity.setCreateTime(date);//时间
                            huodongList.add(huodongEntity);


                            //把要查询是否重复的字段放入map中
                                //活动编号
                                if(seachFields.containsKey("huodongUuidNumber")){
                                    List<String> huodongUuidNumber = seachFields.get("huodongUuidNumber");
                                    huodongUuidNumber.add(data.get(0));//要改的
                                }else{
                                    List<String> huodongUuidNumber = new ArrayList<>();
                                    huodongUuidNumber.add(data.get(0));//要改的
                                    seachFields.put("huodongUuidNumber",huodongUuidNumber);
                                }
                        }

                        //查询是否重复
                         //活动编号
                        List<HuodongEntity> huodongEntities_huodongUuidNumber = huodongService.selectList(new EntityWrapper<HuodongEntity>().in("huodong_uuid_number", seachFields.get("huodongUuidNumber")).eq("huodong_delete", 1));
                        if(huodongEntities_huodongUuidNumber.size() >0 ){
                            ArrayList<String> repeatFields = new ArrayList<>();
                            for(HuodongEntity s:huodongEntities_huodongUuidNumber){
                                repeatFields.add(s.getHuodongUuidNumber());
                            }
                            return R.error(511,"数据库的该表中的 [活动编号] 字段已经存在 存在数据为:"+repeatFields.toString());
                        }
                        huodongService.insertBatch(huodongList);
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
        PageUtils page = huodongService.queryPage(params);

        //字典表数据转换
        List<HuodongView> list =(List<HuodongView>)page.getList();
        for(HuodongView c:list)
            dictionaryService.dictionaryConvert(c, request); //修改对应字典表字段
        return R.ok().put("data", page);
    }

    /**
    * 前端详情
    */
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("detail方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        HuodongEntity huodong = huodongService.selectById(id);
            if(huodong !=null){

                //点击数量加1
                huodong.setHuodongClicknum(huodong.getHuodongClicknum()+1);
                huodongService.updateById(huodong);

                //entity转view
                HuodongView view = new HuodongView();
                BeanUtils.copyProperties( huodong , view );//把实体数据重构到view中

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
    public R add(@RequestBody HuodongEntity huodong, HttpServletRequest request){
        logger.debug("add方法:,,Controller:{},,huodong:{}",this.getClass().getName(),huodong.toString());
        Wrapper<HuodongEntity> queryWrapper = new EntityWrapper<HuodongEntity>()
            .eq("huodong_uuid_number", huodong.getHuodongUuidNumber())
            .eq("huodong_name", huodong.getHuodongName())
            .eq("huodong_address", huodong.getHuodongAddress())
            .eq("huodong_yiqing", huodong.getHuodongYiqing())
            .eq("huodong_types", huodong.getHuodongTypes())
            .eq("huodong_clicknum", huodong.getHuodongClicknum())
            .eq("shangxia_types", huodong.getShangxiaTypes())
            .eq("huodong_delete", huodong.getHuodongDelete())
            ;
        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        HuodongEntity huodongEntity = huodongService.selectOne(queryWrapper);
        if(huodongEntity==null){
            huodong.setHuodongDelete(1);
            huodong.setCreateTime(new Date());
        huodongService.insert(huodong);
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }


}
