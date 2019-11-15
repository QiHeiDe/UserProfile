package cn.itcast.userprofile.platform.repo;

import cn.itcast.userprofile.platform.bean.po.ModelPo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModelRepo extends JpaRepository<ModelPo, Long> {
    ModelPo findByTagId(Long tagId);
}
