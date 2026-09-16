package com.zifang.z.meta.web.api;

import com.zifang.util.core.meta.Result;
import com.zifang.z.meta.core.collector.BizModelCollector;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/meta")
public class BizModelController {

    @Resource
    private BizModelCollector collector;

    @GetMapping("/models")
    public Result<List<BizModelCollector.BizModelDTO>> listModels() {
        return Result.success(collector.getAllModels());
    }

    @GetMapping("/models/{code}")
    public Result<BizModelCollector.BizModelDTO> getModel(@PathVariable String code) {
        BizModelCollector.BizModelDTO dto = collector.getModel(code);
        if (dto == null) return Result.<BizModelCollector.BizModelDTO>fail("资源不存在").code(400);
        return Result.success(dto);
    }

    @GetMapping("/models/module/{module}")
    public Result<List<BizModelCollector.BizModelDTO>> getModelsByModule(@PathVariable String module) {
        return Result.success(collector.getModelsByModule(module));
    }
}
