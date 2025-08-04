#!/bin/bash

# 企业知识库 API 测试脚本
# 使用方法: ./test-api.sh

# 设置基础 URL 和测试参数
BASE_URL="http://localhost:8080"
USER_ID="testuser123"
TEST_CATEGORY="技术文档"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查 jq 是否安装
check_jq() {
    if ! command -v jq &> /dev/null; then
        log_warning "jq 未安装，JSON 响应将不会格式化"
        log_info "安装 jq: brew install jq (macOS) 或 sudo apt-get install jq (Ubuntu)"
        JQ_AVAILABLE=false
    else
        JQ_AVAILABLE=true
    fi
}

# 格式化 JSON 响应
format_json() {
    if [ "$JQ_AVAILABLE" = true ]; then
        jq '.' 2>/dev/null || cat
    else
        cat
    fi
}

# 检查服务是否运行
check_service() {
    log_info "检查服务状态..."
    if curl -s -f "$BASE_URL/actuator/health" > /dev/null; then
        log_success "服务正在运行"
        return 0
    else
        log_error "服务未运行，请先启动应用"
        return 1
    fi
}

# 健康检查
test_health() {
    log_info "1. 健康检查"
    curl -s -X GET "$BASE_URL/actuator/health" | format_json
    echo
}

# 测试文档上传
test_upload() {
    log_info "2. 测试文档上传"
    
    # 创建一个测试文件
    TEST_FILE="test-document.txt"
    echo "这是一个测试文档内容，用于测试企业知识库系统。" > "$TEST_FILE"
    
    UPLOAD_RESPONSE=$(curl -s -X POST "$BASE_URL/api/documents/upload" \
        -H "Content-Type: multipart/form-data" \
        -F "file=@$TEST_FILE" \
        -F "category=$TEST_CATEGORY" \
        -F "userId=$USER_ID")
    
    echo "$UPLOAD_RESPONSE" | format_json
    
    # 清理测试文件
    rm -f "$TEST_FILE"
    echo
}

# 测试获取文档列表
test_get_documents() {
    log_info "3. 获取文档列表"
    curl -s -X GET "$BASE_URL/api/documents?userId=$USER_ID" | format_json
    echo
}

# 测试智能问答
test_query() {
    log_info "4. 测试智能问答"
    
    QUERY_RESPONSE=$(curl -s -X POST "$BASE_URL/api/query" \
        -H "Content-Type: application/json" \
        -d "{
            \"question\": \"什么是企业知识库？\",
            \"userId\": \"$USER_ID\"
        }")
    
    echo "$QUERY_RESPONSE" | format_json
    echo
}

# 测试查询历史
test_query_history() {
    log_info "5. 获取查询历史"
    curl -s -X GET "$BASE_URL/api/query/history?userId=$USER_ID&limit=5" | format_json
    echo
}

# 测试用户注册
test_user_register() {
    log_info "6. 测试用户注册"
    
    REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/users/register" \
        -H "Content-Type: application/json" \
        -d "{
            \"username\": \"testuser\",
            \"email\": \"test@example.com\",
            \"password\": \"password123\"
        }")
    
    echo "$REGISTER_RESPONSE" | format_json
    echo
}

# 测试监控端点
test_monitoring() {
    log_info "7. 测试监控端点"
    
    log_info "应用信息:"
    curl -s -X GET "$BASE_URL/actuator/info" | format_json
    echo
    
    log_info "可用指标:"
    curl -s -X GET "$BASE_URL/actuator/metrics" | format_json
    echo
}

# 主测试函数
main() {
    echo "=========================================="
    echo "    企业知识库 API 测试脚本"
    echo "=========================================="
    echo
    
    # 检查依赖
    check_jq
    
    # 检查服务状态
    if ! check_service; then
        exit 1
    fi
    
    # 执行测试
    test_health
    test_upload
    test_get_documents
    test_query
    test_query_history
    test_user_register
    test_monitoring
    
    echo "=========================================="
    log_success "所有测试完成！"
    echo "=========================================="
}

# 运行主函数
main "$@" 