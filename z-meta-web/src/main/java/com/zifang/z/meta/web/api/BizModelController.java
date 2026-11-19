package com.zifang.z.meta.web.api;

import com.zifang.util.core.meta.Result;
import com.zifang.z.meta.api.annotation.BizApi;
import com.zifang.z.meta.api.annotation.BizField;
import com.zifang.z.meta.api.annotation.BizParam;
import com.zifang.z.meta.core.collector.BizModelCollector;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/meta")
public class BizModelController {

    @Resource
    private BizModelCollector collector;

    @GetMapping("/models")
    @BizApi(summary = "listModels", description = "", tags = { "z-meta" })
    public Result<List<BizModelCollector.BizModelDTO>> listModels() {
        return Result.success(collector.getAllModels());
    }

    @GetMapping("/models/{code}")
    @BizApi(summary = "getModel", description = "", tags = { "z-meta" })
    public Result<BizModelCollector.BizModelDTO> getModel(@PathVariable @BizParam(name = "code", type = BizField.FieldType.STRING, description = "", position = BizParam.Position.PATH) String code) {
        BizModelCollector.BizModelDTO dto = collector.getModel(code);
        if (dto == null) return Result.<BizModelCollector.BizModelDTO>fail("资源不存在").code(400);
        return Result.success(dto);
    }

    @GetMapping("/models/module/{module}")
    @BizApi(summary = "getModelsByModule", description = "", tags = { "z-meta" })
    public Result<List<BizModelCollector.BizModelDTO>> getModelsByModule(@PathVariable @BizParam(name = "module", type = BizField.FieldType.STRING, description = "", position = BizParam.Position.PATH) String module) {
        return Result.success(collector.getModelsByModule(module));
    }
}



