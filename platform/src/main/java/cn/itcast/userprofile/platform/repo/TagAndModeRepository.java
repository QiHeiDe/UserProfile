package cn.itcast.userprofile.platform.repo;

import cn.itcast.userprofile.platform.bean.po.TagPo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TagAndModeRepository extends JpaRepository<TagPo,Long> {

   public List<TagPo> findByNameAndLevelAndPid(String name, Integer level, Long pid);

    /**
     * 根据pid查询
     * @param pid
     * @return
     */
   public List<TagPo> findByPid(Long pid);

    /**
     * 根据等级查询
     * @param level
     * @return
     */
   public List<TagPo> findByLevel(Integer level);
}
