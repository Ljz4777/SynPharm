# SynPharm PPI数据流开发操作文档

## 目录

1. [任务概述](#1-任务概述)
2. [开发步骤](#2-开发步骤)
3. [代码实现](#3-代码实现)
4. [测试验证](#4-测试验证)

---

## 1. 任务概述

### 1.1 目标

创建 **PpiAlgoExecutor**，实现蛋白质-蛋白质相互作用（PPI）预测功能。

### 1.2 所需文件

| 文件 | 状态 | 说明 |
|:---|:---|:---|
| `pipeline/impl/PpiAlgoExecutor.java` | ❌待创建 | PPI算法执行器 |

### 1.3 复用现有组件

以下组件已实现，无需修改：

| 文件 | 功能 | 复用性 |
|:---|:---|:---|
| `pipeline/AlgoExecutor.java` | 算法执行器接口 | ✅通用 |
| `pipeline/InputParser.java` | 输入解析器接口 | ✅通用 |
| `pipeline/OutputFormatter.java` | 输出格式化器接口 | ✅通用 |
| `pipeline/DataPipelineFactory.java` | 数据流工厂 | ✅通用 |
| `pipeline/impl/SmilesInputParser.java` | SMILES输入解析器 | ✅通用 |
| `pipeline/impl/JsonOutputFormatter.java` | JSON输出格式化器 | ✅通用 |
| `client/FastApiClient.java` | FastAPI客户端 | ✅通用 |
| `dto/request/PredictRequest.java` | 预测请求DTO | ✅通用（含forPPI方法） |
| `dto/response/AlgoResponse.java` | 算法响应DTO | ✅通用 |
| `enums/AlgoType.java` | 算法类型枚举 | ✅通用（已包含PPI） |

### 1.4 数据流组合

创建PpiAlgoExecutor后，自动支持以下组合：

| InputType | AlgoType | OutputType | 状态 |
|:---|:---|:---|:---|
| smiles | PPI | json | ✅自动支持 |
| uniprot | PPI | json | ⏳待实现UniprotInputParser |
| pdb | PPI | json | ⏳待实现PdbInputParser |
| csv | PPI | json | ⏳待实现CsvInputParser |

---

## 2. 开发步骤

### ⚠️ 前置条件（必须先完成）

在开发PpiAlgoExecutor之前，以下文件必须已经存在：

| 序号 | 文件 | 状态 | 说明 |
|:---|:---|:---|:---|
| 1 | `enums/AlgoType.java` | ✅ | 算法类型枚举（已包含PPI） |
| 2 | `dto/ParsedInput.java` | ✅ | 统一输入格式DTO |
| 3 | `dto/request/PredictRequest.java` | ✅ | 预测请求DTO（含forPPI方法） |
| 4 | `dto/response/AlgoResponse.java` | ✅ | 算法响应DTO |
| 5 | `pipeline/AlgoExecutor.java` | ✅ | 算法执行器接口 |
| 6 | `client/FastApiClient.java` | ✅ | FastAPI调用客户端 |

### 步骤1：创建PpiAlgoExecutor文件

**文件夹**：`synpharm-backend/src/main/java/com/synpharm/pipeline/impl/`  
**文件**：`PpiAlgoExecutor.java`

### 步骤2：实现AlgoExecutor接口

需要实现三个方法：
1. `getAlgoType()` - 返回算法类型
2. `execute()` - 执行单条预测
3. `batchExecute()` - 执行批量预测

### 步骤3：完成！

Spring启动时自动扫描并注册到工厂，无需额外配置。

---

### 📋 完整时序流程

```
┌─────────────────────────────────────────────────────────────────┐
│                    开发时序流程                                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  阶段1: 基础准备（架构师完成）                                    │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │ ① enums/AlgoType.java          → 定义算法类型枚举        │    │
│  │ ② dto/ParsedInput.java         → 统一输入格式            │    │
│  │ ③ dto/request/PredictRequest.java → 预测请求DTO         │    │
│  │ ④ dto/response/AlgoResponse.java → 算法响应DTO          │    │
│  │ ⑤ pipeline/AlgoExecutor.java   → 算法执行器接口          │    │
│  │ ⑥ client/FastApiClient.java    → FastAPI客户端          │    │
│  └─────────────────────────────────────────────────────────┘    │
│                              ↓                                  │
│  阶段2: 实现算法执行器（组员完成）                                │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │ ⑦ pipeline/impl/PpiAlgoExecutor.java → PPI执行器实现    │    │
│  └─────────────────────────────────────────────────────────┘    │
│                              ↓                                  │
│  阶段3: 自动注册（Spring完成）                                   │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │ Spring启动时自动扫描并注册到DataPipelineFactory          │    │
│  └─────────────────────────────────────────────────────────┘    │
│                              ↓                                  │
│  阶段4: 验证测试（组员完成）                                      │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │ ⑧ 单元测试 → 编译 → 启动 → 接口测试                      │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 3. 代码实现

### 3.1 PpiAlgoExecutor.java

```java
package com.synpharm.pipeline.impl;

import com.synpharm.client.FastApiClient;
import com.synpharm.dto.ParsedInput;
import com.synpharm.dto.request.PredictRequest;
import com.synpharm.dto.response.AlgoResponse;
import com.synpharm.dto.response.BatchPredictionResponse;
import com.synpharm.enums.AlgoType;
import com.synpharm.pipeline.AlgoExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PpiAlgoExecutor implements AlgoExecutor {

    private final FastApiClient fastApiClient;

    @Override
    public AlgoType getAlgoType() {
        return AlgoType.PPI;
    }

    @Override
    public AlgoResponse execute(ParsedInput inputData) {
        log.info("执行PPI算法: params={}", inputData.getParams());
        
        List<String> params = inputData.getParams();
        String proteinA = params.get(0);
        String proteinB = params.get(1);
        
        PredictRequest request = PredictRequest.forPPI(proteinA, proteinB);
        
        AlgoResponse response = fastApiClient.predictSingle(request);
        response.setAlgoType(AlgoType.PPI.getCode());
        
        return response;
    }

    @Override
    public List<AlgoResponse> batchExecute(List<ParsedInput> inputDataList) {
        log.info("批量执行PPI算法: 数量={}", inputDataList.size());
        
        List<PredictRequest> requests = new ArrayList<>();
        for (ParsedInput input : inputDataList) {
            List<String> params = input.getParams();
            requests.add(PredictRequest.forPPI(params.get(0), params.get(1)));
        }
        
        BatchPredictionResponse batchResponse = fastApiClient.predictBatch(requests, AlgoType.PPI.getCode());
        
        List<AlgoResponse> responses = new ArrayList<>();
        if (batchResponse != null && batchResponse.getResults() != null) {
            for (var result : batchResponse.getResults()) {
                AlgoResponse response = new AlgoResponse();
                response.setStatus("success");
                response.setAlgoType(AlgoType.PPI.getCode());
                response.setMetrics(result.getMetrics());
                responses.add(response);
            }
        }
        
        return responses;
    }
}
```

### 3.2 代码讲解

#### 单条执行流程（execute方法）

1. **获取参数**：从 `ParsedInput` 中获取蛋白质序列
   ```java
   List<String> params = inputData.getParams();
   String proteinA = params.get(0);  // 蛋白质A序列
   String proteinB = params.get(1);  // 蛋白质B序列
   ```

2. **构建请求**：使用 `PredictRequest.forPPI()` 静态工厂方法
   ```java
   PredictRequest request = PredictRequest.forPPI(proteinA, proteinB);
   ```

3. **调用FastAPI**：通过 `FastApiClient` 发送请求
   ```java
   AlgoResponse response = fastApiClient.predictSingle(request);
   ```

4. **设置算法类型**：标记响应的算法类型
   ```java
   response.setAlgoType(AlgoType.PPI.getCode());
   ```

#### 批量执行流程（batchExecute方法）

1. **遍历输入列表**：构建批量请求
   ```java
   List<PredictRequest> requests = new ArrayList<>();
   for (ParsedInput input : inputDataList) {
       List<String> params = input.getParams();
       requests.add(PredictRequest.forPPI(params.get(0), params.get(1)));
   }
   ```

2. **调用批量接口**：一次性发送所有请求
   ```java
   BatchPredictionResponse batchResponse = fastApiClient.predictBatch(requests, AlgoType.PPI.getCode());
   ```

3. **转换响应**：将批量响应转换为标准格式
   ```java
   List<AlgoResponse> responses = new ArrayList<>();
   for (var result : batchResponse.getResults()) {
       AlgoResponse response = new AlgoResponse();
       response.setStatus("success");
       response.setAlgoType(AlgoType.PPI.getCode());
       response.setMetrics(result.getMetrics());
       responses.add(response);
   }
   ```

---

## 4. 测试验证

### 4.1 单元测试

```java
package com.synpharm.pipeline.impl;

import com.synpharm.dto.ParsedInput;
import com.synpharm.enums.AlgoType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PpiAlgoExecutorTest {

    @InjectMocks
    private PpiAlgoExecutor executor;

    @Test
    void testGetAlgoType() {
        assertEquals(AlgoType.PPI, executor.getAlgoType());
    }

    @Test
    void testExecute() {
        ParsedInput input = ParsedInput.builder()
                .params(Arrays.asList("MGLGLG...", "MVHLTE..."))
                .inputType("smiles")
                .build();
        
        assertDoesNotThrow(() -> executor.execute(input));
    }

    @Test
    void testBatchExecute() {
        List<ParsedInput> inputs = Arrays.asList(
                ParsedInput.builder().params(Arrays.asList("P1", "P2")).build(),
                ParsedInput.builder().params(Arrays.asList("P3", "P4")).build()
        );
        
        assertDoesNotThrow(() -> executor.batchExecute(inputs));
    }
}
```

### 4.2 接口测试

使用Postman或curl测试：

```bash
POST /api/predict/general
Content-Type: application/json

{
    "inputType": "smiles",
    "algoType": "PPI",
    "outputType": "json",
    "inputValue": "MGLGLG...(蛋白质A序列),MVHLTE...(蛋白质B序列)"
}
```

### 4.3 验证步骤

1. ✅ 创建 `PpiAlgoExecutor.java` 文件
2. ✅ 编写代码，实现 `AlgoExecutor` 接口
3. ✅ 编译项目，确保无错误
4. ✅ 启动应用，查看日志：`注册算法执行器: PPI`
5. ✅ 调用预测接口，确认返回正确格式
6. ✅ 测试异常输入，确认错误处理

---

## 附录：参考文件

| 文件 | 路径 | 说明 |
|:---|:---|:---|
| 参考示例 | `pipeline/impl/DtiAlgoExecutor.java` | DTI算法执行器实现（已完成） |
| 接口定义 | `pipeline/AlgoExecutor.java` | 算法执行器接口 |
| 请求DTO | `dto/request/PredictRequest.java` | 预测请求数据结构 |
| 响应DTO | `dto/response/AlgoResponse.java` | 算法响应数据结构 |
| 客户端 | `client/FastApiClient.java` | FastAPI调用客户端 |

---

**版本**: v1.0.0  
**创建日期**: 2026-07-26  
**任务状态**: 待开发  
**负责人**: 组员

---

## 与DTI的差异

| 对比项 | DTI | PPI |
|:---|:---|:---|
| 算法类型 | `AlgoType.DTI` | `AlgoType.PPI` |
| 请求构建 | `PredictRequest.forDTI()` | `PredictRequest.forPPI()` |
| 参数1 | 药物SMILES | 蛋白质A序列 |
| 参数2 | 蛋白质序列 | 蛋白质B序列 |
| 其他 | 完全相同 | 完全相同 |

**核心差异只有3处**：
1. `getAlgoType()` 返回值
2. `PredictRequest.forPPI()` 方法调用
3. 参数含义（蛋白质A/B vs 药物/靶点）

其余代码结构、逻辑完全一致！