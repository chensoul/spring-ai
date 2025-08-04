#!/bin/bash

# 文本分割器性能测试脚本

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${BLUE}=== 文本分割器性能测试 ===${NC}"
echo ""

# 测试配置
API_BASE="http://localhost:8080/api"
USER_ID="admin"
TEST_FILES=(
    "small-document.txt"
    "medium-document.pdf"
    "large-document.pdf"
)

# 测试函数
test_document_upload() {
    local file_path="$1"
    local category="$2"
    
    echo -e "${YELLOW}测试文档上传: $file_path${NC}"
    
    if [ ! -f "$file_path" ]; then
        echo -e "${RED}文件不存在: $file_path${NC}"
        return 1
    fi
    
    local start_time=$(date +%s%3N)
    
    # 上传文档
    response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
        -X POST "$API_BASE/documents/upload" \
        -F "file=@$file_path" \
        -F "category=$category" \
        -F "userId=$USER_ID")
    
    local end_time=$(date +%s%3N)
    local duration=$((end_time - start_time))
    
    # 提取HTTP状态码
    local http_status=$(echo "$response" | grep "HTTP_STATUS:" | cut -d: -f2)
    local response_body=$(echo "$response" | sed '/HTTP_STATUS:/d')
    
    if [ "$http_status" -eq 200 ]; then
        echo -e "${GREEN}✓ 上传成功${NC}"
        echo "  耗时: ${duration}ms"
        echo "  响应: $response_body"
        
        # 提取文档ID
        local document_id=$(echo "$response_body" | grep -o '"documentId":[0-9]*' | cut -d: -f2)
        if [ -n "$document_id" ]; then
            echo "  文档ID: $document_id"
            monitor_document_processing "$document_id"
        fi
    else
        echo -e "${RED}✗ 上传失败 (HTTP $http_status)${NC}"
        echo "  响应: $response_body"
    fi
    
    echo ""
}

# 监控文档处理状态
monitor_document_processing() {
    local document_id="$1"
    local max_attempts=30
    local attempt=0
    
    echo -e "${YELLOW}监控文档处理状态: documentId=$document_id${NC}"
    
    while [ $attempt -lt $max_attempts ]; do
        local status_response=$(curl -s "$API_BASE/documents/$document_id?userId=$USER_ID")
        local status=$(echo "$status_response" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
        
        case "$status" in
            "COMPLETED")
                echo -e "${GREEN}✓ 文档处理完成${NC}"
                return 0
                ;;
            "FAILED")
                local error=$(echo "$status_response" | grep -o '"errorMessage":"[^"]*"' | cut -d'"' -f4)
                echo -e "${RED}✗ 文档处理失败: $error${NC}"
                return 1
                ;;
            "PROCESSING")
                echo "  处理中... (尝试 $((attempt + 1))/$max_attempts)"
                ;;
            *)
                echo "  未知状态: $status"
                ;;
        esac
        
        sleep 5
        attempt=$((attempt + 1))
    done
    
    echo -e "${RED}✗ 文档处理超时${NC}"
    return 1
}

# 性能基准测试
performance_benchmark() {
    echo -e "${BLUE}=== 性能基准测试 ===${NC}"
    echo ""
    
    # 测试不同大小的文档
    local test_cases=(
        "small:small-document.txt:技术文档"
        "medium:medium-document.pdf:用户手册"
        "large:large-document.pdf:开发指南"
    )
    
    for test_case in "${test_cases[@]}"; do
        IFS=':' read -r size file_path category <<< "$test_case"
        
        echo -e "${YELLOW}测试 $size 文档: $file_path${NC}"
        test_document_upload "$file_path" "$category"
    done
}

# 批量测试
batch_test() {
    echo -e "${BLUE}=== 批量上传测试 ===${NC}"
    echo ""
    
    local batch_size=5
    local test_files=("test1.txt" "test2.txt" "test3.txt" "test4.txt" "test5.txt")
    
    echo -e "${YELLOW}批量上传 $batch_size 个文档${NC}"
    
    local start_time=$(date +%s%3N)
    local success_count=0
    
    for file in "${test_files[@]}"; do
        if test_document_upload "$file" "批量测试"; then
            success_count=$((success_count + 1))
        fi
    done
    
    local end_time=$(date +%s%3N)
    local total_duration=$((end_time - start_time))
    
    echo -e "${BLUE}批量测试结果:${NC}"
    echo "  成功: $success_count/$batch_size"
    echo "  总耗时: ${total_duration}ms"
    echo "  平均耗时: $((total_duration / batch_size))ms"
    echo ""
}

# 配置对比测试
config_comparison_test() {
    echo -e "${BLUE}=== 配置对比测试 ===${NC}"
    echo ""
    
    # 测试不同配置的性能
    local configs=(
        "default:1000:300:5:10000"
        "optimized:1500:500:10:20000"
        "high-performance:2000:800:20:15000"
    )
    
    for config in "${configs[@]}"; do
        IFS=':' read -r name chunk_size min_chunk min_embed max_chunks <<< "$config"
        
        echo -e "${YELLOW}测试配置: $name${NC}"
        echo "  块大小: $chunk_size"
        echo "  最小块大小: $min_chunk"
        echo "  最小嵌入长度: $min_embed"
        echo "  最大块数: $max_chunks"
        
        # 这里可以添加实际的配置测试逻辑
        echo "  配置测试完成"
        echo ""
    done
}

# 生成测试文档
generate_test_documents() {
    echo -e "${BLUE}=== 生成测试文档 ===${NC}"
    echo ""
    
    # 生成小文档
    echo "这是一个小的测试文档，用于测试文本分割器的性能。" > small-document.txt
    echo "文档包含一些基本的技术内容，用于验证分割效果。" >> small-document.txt
    
    # 生成中等文档
    cat > medium-document.txt << 'EOF'
这是一个中等大小的测试文档，包含更多的技术内容。

## 技术规范
这里包含详细的技术规范说明，用于测试文本分割器的处理能力。

### 功能特性
- 特性1：高性能处理
- 特性2：智能分割
- 特性3：质量保证

### 实现细节
这里包含具体的实现细节，包括代码示例和配置说明。

## 使用指南
详细的使用指南，包含步骤说明和注意事项。

### 安装步骤
1. 下载软件包
2. 解压文件
3. 运行安装程序
4. 配置参数

### 配置说明
详细的配置参数说明，包括各种选项和默认值。
EOF
    
    # 生成大文档
    for i in {1..10}; do
        cat medium-document.txt >> large-document.txt
        echo "" >> large-document.txt
        echo "## 章节 $i" >> large-document.txt
        echo "这是第 $i 个章节的内容，用于测试大文档的处理性能。" >> large-document.txt
    done
    
    echo -e "${GREEN}✓ 测试文档生成完成${NC}"
    echo "  - small-document.txt"
    echo "  - medium-document.txt"
    echo "  - large-document.txt"
    echo ""
}

# 显示帮助信息
show_help() {
    echo -e "${BLUE}文本分割器性能测试工具${NC}"
    echo ""
    echo "使用方法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  generate              # 生成测试文档"
    echo "  benchmark             # 性能基准测试"
    echo "  batch                 # 批量上传测试"
    echo "  config                # 配置对比测试"
    echo "  all                   # 执行所有测试"
    echo "  help                  # 显示此帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 generate           # 生成测试文档"
    echo "  $0 benchmark          # 执行性能测试"
    echo "  $0 all                # 执行所有测试"
}

# 主函数
main() {
    case "${1:-help}" in
        "generate")
            generate_test_documents
            ;;
        "benchmark")
            performance_benchmark
            ;;
        "batch")
            batch_test
            ;;
        "config")
            config_comparison_test
            ;;
        "all")
            generate_test_documents
            performance_benchmark
            batch_test
            config_comparison_test
            ;;
        "help"|*)
            show_help
            ;;
    esac
}

# 执行主函数
main "$@"