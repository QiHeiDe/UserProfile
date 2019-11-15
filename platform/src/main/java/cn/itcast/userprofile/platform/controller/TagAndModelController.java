package cn.itcast.userprofile.platform.controller;

import cn.itcast.up.common.HdfsTools;
import cn.itcast.userprofile.platform.bean.dto.ModelDto;
import cn.itcast.userprofile.platform.bean.dto.TagDto;
import cn.itcast.userprofile.platform.bean.dto.TagModelDto;
import cn.itcast.userprofile.platform.service.TagAndModeService;
import cn.itcast.userprofile.platform.tools.Codes;
import cn.itcast.userprofile.platform.tools.HttpResult;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@RestController
public class TagAndModelController {
    @Autowired
    private TagAndModeService tagAndModeService;
    @PutMapping("tags/relation")
    public void putTags(@RequestBody List<TagDto> tags){
        System.out.println(tags);
        tagAndModeService.putTags(tags);
    }

    @GetMapping("tags")
    public HttpResult<List<TagDto>> getTagByLevelOrId(@RequestParam(required = false) Integer level,
                                                      @RequestParam(required = false)  Long pid){
        List<TagDto> list = null;
        if (level == null && pid != null) {
            //根据ID查找
            list = tagAndModeService.findByPid(pid);
        }
        if (level != null && pid == null) {
            //根据等级查找查找
            list = tagAndModeService.findByLevel(level);
        }
        return new HttpResult<List<TagDto>>(1,"查询成功", list);
    }

    /**
     * 四级界面新增标签模型
     * @param tagModelDto
     * @return
     */
    @PutMapping("tags/model")
    public HttpResult putModel(@RequestBody TagModelDto tagModelDto){
        System.out.println(tagModelDto);
        tagAndModeService.addTagModel(tagModelDto.getTag(), tagModelDto.getModel());
        return new HttpResult(Codes.SUCCESS, "成功", null);
    }

    /**
     * 展示4级标签
     * @param pid
     * @return
     */
    @GetMapping("tags/model")
    public HttpResult getModel(Long pid){
        List<TagModelDto> dto = tagAndModeService.findModelByPid(pid);
        return new HttpResult(Codes.SUCCESS, "查询成功", dto);
    }
    //5级标签
    @PutMapping("tags/data")
    public HttpResult putData(@RequestBody TagDto tagDto){
        tagAndModeService.addDataTag(tagDto);
        return new HttpResult(Codes.SUCCESS, "添加成功", null);
    }

    /**
     * 启动/停止模型
     * @param id
     * @param modelDto
     * @return
     */
    @PostMapping("tags/{id}/model")
    public HttpResult changeModelState(@PathVariable Long id, @RequestBody ModelDto modelDto){
        tagAndModeService.updateModelState(id, modelDto.getState());
        return new HttpResult(Codes.SUCCESS, "执行成功", null);
    }


    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/tags/upload")
    public HttpResult<String> postTagsFile(@RequestParam("file") MultipartFile file) {
        String basePath = "/temp/jars/";
        //创建Jar包名字
        String fileName = UUID.randomUUID().toString() + ".jar";
        String path = basePath + fileName;
//        cn.itcast.up29.TestTag
        try {
            InputStream inputStream = file.getInputStream();
            IOUtils.copy(inputStream, new FileOutputStream(new File("a.jar")));
            HdfsTools.build().uploadLocalFile2HDFS("a.jar",path);
            System.out.println("hdfs://bd001:8020"+path);
            return new HttpResult<>(Codes.SUCCESS, "", "hdfs://bd001:8020"+path);
        } catch (IOException e) {
            e.printStackTrace();
            return new HttpResult<>(Codes.ERROR, "文件上传失败", null);
        }
    }

}
