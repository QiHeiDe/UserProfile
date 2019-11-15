package cn.itcast.userprofile.platform.service;

import cn.itcast.userprofile.platform.bean.dto.ModelDto;
import cn.itcast.userprofile.platform.bean.dto.TagDto;
import cn.itcast.userprofile.platform.bean.dto.TagModelDto;
import cn.itcast.userprofile.platform.bean.po.ModelPo;
import cn.itcast.userprofile.platform.bean.po.TagPo;
import cn.itcast.userprofile.platform.repo.ModelRepo;
import cn.itcast.userprofile.platform.repo.TagAndModeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagAndModeServiceImpl implements TagAndModeService {
    @Autowired
    private TagAndModeRepository tagAndModeRepository;
    @Autowired
    private ModelRepo modelRepo;
    @Autowired
    private Engine engine;
    @Override
    public void putTags(List<TagDto> tags) {
        //先排序
        tags.sort((tag1,tag2)->{
            if(tag1.getLevel()>tag2.getLevel()){
                return 1;
            }
            if(tag1.getLevel()<tag2.getLevel()){
                return -1;
            }
            return 0;
        });

        //开始将数据保存
        TagPo tmpPo = null;
        for (TagDto tag : tags) {
            //将dto转换为po,方便后面数据操作
            TagPo tagPo = convert(tag);
            if(tmpPo==null){
                //肯定是第一次循环,此时肯定是1级标签
                List<TagPo> list = tagAndModeRepository.findByNameAndLevelAndPid(tagPo.getName(), tagPo.getLevel(), tagPo.getPid());
                if(list==null || list.size()==0){
                    //没有查询到,直接保存
                    tmpPo = tagAndModeRepository.save(tagPo);
                }else {
                    //如果查到,那么直接将值取出来
                    tmpPo = list.get(0);
                }
            }else {
                //tmpPo不等于null说明之前存储过了
                //如果进入else说明肯定是2 3级标签
                tagPo.setPid(tmpPo.getId());
                //我们可以使用标签名称/等级/父id进行查询,存在就不保存了
                List<TagPo> list = tagAndModeRepository.findByNameAndLevelAndPid(tagPo.getName(), tagPo.getLevel(), tagPo.getPid());
                if(list==null || list.size()==0){
                    //没有查到直接保存
                    tmpPo = tagAndModeRepository.save(tagPo);
                }else {
                    //如果查到,将数据库的数据复制给临时对象
                    tmpPo = list.get(0);
                }
            }
        }
    }

    @Override
    public List<TagDto> findByPid(Long pid) {
        List<TagPo> list = tagAndModeRepository.findByPid(pid);
        List<TagDto> tagDtolist = list.stream().map(this::convert).collect(Collectors.toList());
        return tagDtolist;
    }

    @Override
    public List<TagDto> findByLevel(Integer level) {
        List<TagPo> list = tagAndModeRepository.findByLevel(level);
        List<TagDto> tagDtos = list.stream().map(this::convert).collect(Collectors.toList());
        return tagDtos;
    }

    /**
     * 添加4级标签
     * @param tagDto
     * @param modelDto
     */
    @Override
    public void addTagModel(TagDto tagDto, ModelDto modelDto) {
        TagPo tagPo = tagAndModeRepository.save(convert(tagDto));
        modelRepo.save(convert(modelDto, tagPo.getId()));
    }
    //展示4级标签
    @Override
    public List<TagModelDto> findModelByPid(Long pid) {
        List<TagPo> tagPos = tagAndModeRepository.findByPid(pid);
        return tagPos.stream().map((tagPo) -> {
            Long id = tagPo.getId();
            ModelPo modelPo = modelRepo.findByTagId(id);
            if (modelPo == null) {
                //找不到model,就只返回tag
                return new TagModelDto(convert(tagPo),null);
            }
            return new TagModelDto(convert(tagPo), convert(modelPo));
        }).collect(Collectors.toList());

    }
    //添加5级标签
    @Override
    public void addDataTag(TagDto tagDto) {
        tagAndModeRepository.save(convert(tagDto));
    }

    @Override
    public void updateModelState(Long id, Integer state) {
        ModelPo modelPo = modelRepo.findByTagId(id);
        //如果传递过来的状态是3,那么就是启动,如果是4那么就是停止

        if (state == ModelPo.STATE_ENABLE) {
            //启动流程
            engine.startModel(convert(modelPo));
        }
        if (state == ModelPo.STATE_DISABLE) {
            //关闭流程
            engine.stopModel(convert(modelPo));
        }
        //更新状态信息
        modelPo.setState(state);
        modelRepo.save(modelPo);
    }


    private ModelDto convert(ModelPo modelPo) {
        ModelDto modelDto = new ModelDto();
        modelDto.setId(modelPo.getId());
        modelDto.setName(modelPo.getName());
        modelDto.setMainClass(modelPo.getMainClass());
        modelDto.setPath(modelPo.getPath());
        modelDto.setArgs(modelPo.getArgs());
        modelDto.setState(modelPo.getState());
        modelDto.setSchedule(modelDto.parseDate(modelPo.getSchedule()));
        return modelDto;
    }


    private ModelPo convert(ModelDto modelDto, Long id) {
        ModelPo modelPo = new ModelPo();
        modelPo.setId(modelDto.getId());
        modelPo.setTagId(id);
        modelPo.setName(modelDto.getName());
        modelPo.setMainClass(modelDto.getMainClass());
        modelPo.setPath(modelDto.getPath());
        modelPo.setSchedule(modelDto.getSchedule().toPattern());
        modelPo.setCtime(new Date());
        modelPo.setUtime(new Date());
        modelPo.setState(modelDto.getState());
        modelPo.setArgs(modelDto.getArgs());
        return modelPo;
    }


    //TagDto转换TagPo
    private TagPo convert(TagDto tagDto){
        TagPo tagPo = new TagPo();
        tagPo.setId(tagDto.getId());
        tagPo.setName(tagDto.getName());
        tagPo.setRule(tagDto.getRule());
        tagPo.setLevel(tagDto.getLevel());
        if(tagDto.getLevel()==1){
            //如果当前等级是1,那么设置父id为-1
            tagPo.setPid(-1L);
        }else {
            tagPo.setPid(tagDto.getPid());
        }
        tagPo.setCtime(new Date());
        tagPo.setUtime(new Date());
        return tagPo;
    }
    //tagPo转换TagDto
    private TagDto convert(TagPo tagPo) {
        TagDto tagDto = new TagDto();
        tagDto.setId(tagPo.getId());
        tagDto.setPid(tagPo.getPid());
        tagDto.setLevel(tagPo.getLevel());
        tagDto.setName(tagPo.getName());
        tagDto.setRule(tagPo.getRule());
        return tagDto;
    }

}
