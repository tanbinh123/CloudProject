package com.staging.controller;


import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.staging.common.Pager;
import com.staging.common.PagerLayui;
import com.staging.common.ServerResponse;
import com.staging.common.constant.ServerResponseConstant;
import com.staging.common.enums.MIMETypeEnum;
import com.staging.common.utils.DeleteFileUtil;
import com.staging.common.utils.FileUtils;
import com.staging.entity.CaseFile;
import com.staging.entity.Notice;
import com.staging.entity.User;
import com.staging.entity.Video;
import com.staging.entity.vo.CaseFileVo;
import com.staging.entity.vo.VideoVo;
import com.staging.service.VideoService;
import com.staging.shiro.config.utils.ShiroUtils;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Animo123
 * @since 2018-07-06
 */
@Controller
@RequestMapping("/video")
public class VideoController {

    private final Logger logger = LoggerFactory.getLogger(VideoController.class);

    @Autowired
    private VideoService videoService;

    /**
     * @Author: 95DBC
     * @Date: 2018/8/11 16:39
     * @Description:教学视频的管理
     *
     */
    @GetMapping("page")
    public String page(){
        return "video/video";
    }

    /**
     * @Author: 95DBC
     * @Date: 2018/8/11 16:40
     * @Description:添加视频的页面
     *
     */
    @GetMapping("addUpdateVideo")
    public String addUpdateVideo(){
        return "video/addUpdateVideo";
    }

    /**
     * @Author: 95DBC
     * @Date: 2018/8/19 14:29
     * @Description: 查看课视频的页面跳转
     *
     */
    @GetMapping("article")
    public String Article(){
        return "video/article";
    }

    @GetMapping("PreviewVideo")
    public String PreviewVideo(){
        return "video/PreviewVideo";
    }

    @PostMapping("pager")
    @ApiOperation("分页查询")
    @ResponseBody
    public Pager pager(Integer page, Integer limit, VideoVo videoVo){
        logger.info("进入作品分页查询:"+videoVo.toString());
        Pager p = new Pager(page,limit);
        p.setRows(videoService.queryPageVideo(p,videoVo));
        p.setTotal(Long.valueOf(videoService.queryPageCount(videoVo)));
        return p;
    }


    @PostMapping("addVideoupload")
    @ApiOperation("添加视频")
    @ResponseBody
    public ServerResponse<CaseFile> addnewupload(MultipartFile fileImg, MultipartFile fileVideo, Video video, HttpServletRequest request){
        User user = ShiroUtils.getUserSession(request);
        if(StringUtils.isEmpty(user)){
            return ServerResponse.createByError("你的登入信息已过期请刷新页面重写登入");
        }
        if(!StringUtils.isEmpty(fileImg)&&!StringUtils.isEmpty(fileVideo)){
            String img = FileUtils.getExtensionWithoutDot(fileImg.getOriginalFilename());
            String vid = FileUtils.getExtensionWithoutDot(fileVideo.getOriginalFilename());

            if((MIMETypeEnum.JPEG.getValue().equals(img) || MIMETypeEnum.JPG.getValue().equals(img)|| MIMETypeEnum.PNG.getValue().equals(img))||
                    MIMETypeEnum.MP4.getValue().equals(vid)|| MIMETypeEnum.WebM.getValue().equals(vid)|| MIMETypeEnum.Ogg.getValue().equals(vid)){
                String pathImg = FileUtils.uploadPath(request,"imgVideo",user.getUserName()+"/");//把用户的图片存放到用户的imgCase文件夹下
                String pathVideo = FileUtils.uploadPath(request,"Video",user.getUserName()+"/");//把用户的3d文件存放到用户的StlCase文件夹下
                try {
                    String imgName = FileUtils.uploadFile(fileImg, pathImg);
                    video.setImgPath("/upload/"+user.getUserName()+"/imgVideo/"+imgName);

                    String stlName = FileUtils.uploadFile(fileVideo, pathVideo);
                    video.setVideoPath("/upload/"+user.getUserName()+"/Video/"+stlName);

                    int size = (int) fileVideo.getSize();
                    video.setSize(size+"");
                    video.setVideoFormat(vid);
                    video.setUid(user.getId());
                    video.setUpDate(Calendar.getInstance().getTime());

                    video.setIschecked(1);
                    videoService.insert(video);
                    return ServerResponse.createBySuccess(ServerResponseConstant.SERVERRESPONSE_SUCCESS_SAVE);
                } catch (IOException e) {
                    e.printStackTrace();
                    return ServerResponse.createByError("你上传的文件格式不正确");
                }
            }else {
                return ServerResponse.createByError("请上传正确的文件");
            }
        }
        return ServerResponse.createByError(ServerResponseConstant.SERVERRESPONSE_ERROR_SAVE);
    }

    @PostMapping("updateVideo")
    @ApiOperation("更新视频")
    @ResponseBody
    public ServerResponse<CaseFile> updateCase(MultipartFile fileImg, MultipartFile fileVideo, Video video, String deletImg,
                                               String deletfileVideo , HttpServletRequest request){
        User user = ShiroUtils.getUserSession(request);
        if(StringUtils.isEmpty(user)){
            return ServerResponse.createByError("你的登入信息已过期请刷新页面重写登入");
        }
        try {
            updateImg(fileImg, video, deletImg, request, user);
            updateVideo(fileVideo, video, deletfileVideo, request, user);
        } catch (IOException e) {
            e.printStackTrace();
            return ServerResponse.createByError("上传错误");
        }
        video.setIschecked(1);
        videoService.updateById(video);
        return ServerResponse.createBySuccess(ServerResponseConstant.SERVERRESPONSE_SUCCESS_UPDATE);
    }

    @PostMapping("deletVideo")
    @ApiOperation("删除教学视频")
    @ResponseBody
    public ServerResponse<CaseFile> deletWorks(Video video){
        if(StringUtils.isEmpty(video.getImgPath())){
            //如果图片路径为空就让DeleteFileUtil.delete删除一个名为null文件夹，这样就不会出现只删除/static/下的所有文件，而是删除/static/null下的文件夹
            video.setImgPath("null");
        }
        if(StringUtils.isEmpty(video.getVideoPath())){
            video.setVideoPath("null");
        }

        return video.deleteById()? DeleteFileUtil.delete(FileUtils.getClasspath()+"static"+video.getImgPath())&&
                DeleteFileUtil.delete(FileUtils.getClasspath()+"static"+video.getVideoPath())?
                ServerResponse.createBySuccess(ServerResponseConstant.SERVERRESPONSE_SUCCESS_DELET):
                ServerResponse.createBySuccess(ServerResponseConstant.SERVERRESPONSE_SUCCESS_DELET)
                :ServerResponse.createByError(ServerResponseConstant.SERVERRESPONSE_ERROR_DELET);
    }

    @PostMapping("updateStatus")
    @ApiOperation("冻结或激活作品")
    @ResponseBody
    public ServerResponse<CaseFile> updateStatus(Video video){
        video.setCheckDate(Calendar.getInstance().getTime());
        return video.getIschecked()==2&&video.updateById()?ServerResponse.createBySuccess(ServerResponseConstant.SERVERRESPONSE_SUCCESS_STATUS)
                :video.getIschecked()==3&&video.updateById()?ServerResponse.createBySuccess(ServerResponseConstant.SERVERRESPONSE_SUCCESS_FREEZE):
                ServerResponse.createByError("操作失败");
    }

    private void updateVideo(MultipartFile fileVideo, Video video, String deletfileVideo, HttpServletRequest request, User user) throws IOException {
        if(!StringUtils.isEmpty(fileVideo)){
            String vid = FileUtils.getExtensionWithoutDot(fileVideo.getOriginalFilename());
            if(MIMETypeEnum.MP4.getValue().equals(vid)|| MIMETypeEnum.WebM.getValue().equals(vid)|| MIMETypeEnum.Ogg.getValue().equals(vid)){

                if(!StringUtils.isEmpty(deletfileVideo)){
                    if(deletfileVideo.equals(" ")){
                        deletfileVideo="null";
                    }
                    DeleteFileUtil.delete(FileUtils.getClasspath()+"static"+deletfileVideo);//删除原来视频文件
                }
                String pathVideo = FileUtils.uploadPath(request,"Video",user.getUserName()+"/");//把用户的3d文件存放到用户的StlCase文件夹下
                String stlName = FileUtils.uploadFile(fileVideo, pathVideo);
                video.setVideoPath("/upload/"+user.getUserName()+"/Video/"+stlName);
            }
        }
    }

    private void updateImg(MultipartFile fileImg, Video video, String deletImg, HttpServletRequest request, User user) throws IOException {
        if(!StringUtils.isEmpty(fileImg)){
            String img = FileUtils.getExtensionWithoutDot(fileImg.getOriginalFilename());
            if(MIMETypeEnum.JPEG.getValue().equals(img) || MIMETypeEnum.JPG.getValue().equals(img)|| MIMETypeEnum.PNG.getValue().equals(img)){
                if(!StringUtils.isEmpty(deletImg)){
                    DeleteFileUtil.delete(FileUtils.getClasspath()+"static"+deletImg);//删除原来图片
                }
                String pathImg = FileUtils.uploadPath(request,"imgVideo",user.getUserName()+"/");//把用户的图片存放到用户的imgCase文件夹下
                String imgName = FileUtils.uploadFile(fileImg, pathImg);
                video.setImgPath("/upload/"+user.getUserName()+"/imgVideo/"+imgName);
            }
        }
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
//        System.out.println("============处理所有@RequestMapping注解方法，在其执行之前初始化数据绑定器");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//        dateFormat.setLenient(false);//这句一个不要存在，不然还是处理不了时间转换
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }
}

