package com.liukx.expression.engine.server.model.dto.request;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
//@Tag(name="逻辑删除请求类")
public class DeleteByIdListRequest implements Serializable {
    //    @Schema(description = "待删除的id列表", required = true)
//    @Size(min = 1, max = 200, message = "id个数1～200个，只能包含数字")
    private List<Long> idList;

    //    @Schema(description = "更新人")
    private String updateBy;
}
