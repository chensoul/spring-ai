#!/bin/bash

# 企业知识库系统启动脚本
# 作者: Spring AI Demo
# 版本: 1.0.0

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_debug() {
    echo -e "${BLUE}[DEBUG]${NC} $1"
}

# 显示横幅
show_banner() {
    echo -e "${BLUE}"
    cat << "EOF"
========================================
🚀 企业知识库系统 (Enterprise KB)
========================================
基于Spring AI的智能知识管理平台

功能特性:
• RAG智能问答
• 文档上传处理  
• 向量化搜索
• 用户权限管理

版本: 1.0.0
========================================
EOF
    echo -e "${NC}"
}

# 检查系统要求
check_requirements() {
    log_info "检查系统要求..."
    
    # 检查Java
    if ! command -v java &> /dev/null; then
        log_error "Java未安装，请安装Java 17或更高版本"
        exit 1
    fi
    
    local java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
    if [ "$java_version" -lt 17 ]; then
        log_error "Java版本过低，需要Java 17或更高版本，当前版本: $java_version"
        exit 1
    fi
    log_info "Java版本检查通过: $(java -version 2>&1 | head -n 1)"
    
    # 检查Maven
    if ! command -v mvn &> /dev/null; then
        log_error "Maven未安装，请安装Maven 3.6或更高版本"
        exit 1
    fi
    log_info "Maven版本: $(mvn -version | head -n 1)"
    
    # 检查Redis（可选）
    if command -v redis-cli &> /dev/null; then
        if redis-cli ping &> /dev/null; then
            log_info "Redis连接正常"
        else
            log_warn "Redis未运行，将使用内存向量存储"
        fi
    else
        log_warn "Redis未安装，将使用内存向量存储"
    fi
}

# 检查环境变量
check_environment() {
    log_info "检查环境变量..."
    
    if [ -z "$OPENAI_API_KEY" ]; then
        log_warn "OPENAI_API_KEY未设置，AI功能可能无法正常工作"
        read -p "是否继续启动? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            log_info "启动已取消"
            exit 0
        fi
    else
        log_info "OPENAI_API_KEY已设置"
    fi
    
    # 设置默认环境变量
    export SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-dev}
    export SERVER_PORT=${SERVER_PORT:-8080}
    export DATABASE_URL=${DATABASE_URL:-jdbc:h2:mem:testdb}
    
    log_info "当前配置文件: $SPRING_PROFILES_ACTIVE"
    log_info "服务端口: $SERVER_PORT"
}

# 构建项目
build_project() {
    log_info "构建项目..."
    
    if [ "$1" = "--skip-tests" ]; then
        log_info "跳过测试，快速构建..."
        mvn clean package -DskipTests -q
    else
        log_info "运行测试并构建..."
        mvn clean package -q
    fi
    
    if [ $? -eq 0 ]; then
        log_info "项目构建成功"
    else
        log_error "项目构建失败"
        exit 1
    fi
}

# 启动应用
start_application() {
    log_info "启动应用..."
    
    local jar_file=$(find target -name "*.jar" -not -name "*sources.jar" -not -name "*javadoc.jar" | head -n 1)
    
    if [ -z "$jar_file" ]; then
        log_error "找不到JAR文件，请先构建项目"
        exit 1
    fi
    
    log_info "启动JAR文件: $jar_file"
    
    # 创建日志目录
    mkdir -p logs
    
    # 启动应用
    java -jar "$jar_file" \
        --spring.profiles.active="$SPRING_PROFILES_ACTIVE" \
        --server.port="$SERVER_PORT" \
        --logging.file.name=logs/application.log \
        2>&1 | tee logs/startup.log &
    
    local app_pid=$!
    echo $app_pid > app.pid
    
    log_info "应用正在启动，PID: $app_pid"
    log_info "日志文件: logs/application.log"
    
    # 等待应用启动
    log_info "等待应用启动..."
    local max_wait=60
    local wait_time=0
    
    while [ $wait_time -lt $max_wait ]; do
        if curl -s http://localhost:$SERVER_PORT/api/actuator/health > /dev/null 2>&1; then
            log_info "应用启动成功！"
            show_access_info
            return 0
        fi
        
        sleep 2
        wait_time=$((wait_time + 2))
        echo -n "."
    done
    
    echo
    log_error "应用启动超时，请检查日志文件"
    return 1
}

# 显示访问信息
show_access_info() {
    echo
    log_info "应用访问信息:"
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}🌐 应用首页:${NC} http://localhost:$SERVER_PORT/api"
    echo -e "${GREEN}💚 健康检查:${NC} http://localhost:$SERVER_PORT/api/actuator/health"
    echo -e "${GREEN}🗄️  H2控制台:${NC} http://localhost:$SERVER_PORT/api/h2-console"
    echo -e "${GREEN}📚 API文档:${NC} http://localhost:$SERVER_PORT/api/swagger-ui.html"
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}🔑 默认账户:${NC}"
    echo -e "${GREEN}   管理员:${NC} admin / admin123"
    echo -e "${GREEN}   用户:${NC} user / user123"
    echo -e "${GREEN}========================================${NC}"
    echo
}

# 停止应用
stop_application() {
    log_info "停止应用..."
    
    if [ -f app.pid ]; then
        local pid=$(cat app.pid)
        if ps -p $pid > /dev/null 2>&1; then
            kill $pid
            log_info "应用已停止 (PID: $pid)"
            rm -f app.pid
        else
            log_warn "应用进程不存在 (PID: $pid)"
            rm -f app.pid
        fi
    else
        log_warn "找不到PID文件，尝试通过端口查找进程..."
        local pid=$(lsof -ti:$SERVER_PORT)
        if [ -n "$pid" ]; then
            kill $pid
            log_info "应用已停止 (PID: $pid)"
        else
            log_warn "未找到运行在端口 $SERVER_PORT 的进程"
        fi
    fi
}

# 重启应用
restart_application() {
    log_info "重启应用..."
    stop_application
    sleep 3
    start_application
}

# 查看状态
show_status() {
    log_info "检查应用状态..."
    
    if [ -f app.pid ]; then
        local pid=$(cat app.pid)
        if ps -p $pid > /dev/null 2>&1; then
            log_info "应用正在运行 (PID: $pid)"
            
            # 检查健康状态
            if curl -s http://localhost:$SERVER_PORT/api/actuator/health > /dev/null 2>&1; then
                log_info "应用健康状态: 正常"
            else
                log_warn "应用健康状态: 异常"
            fi
        else
            log_warn "PID文件存在但进程不存在"
            rm -f app.pid
        fi
    else
        log_info "应用未运行"
    fi
}

# 查看日志
show_logs() {
    if [ -f logs/application.log ]; then
        tail -f logs/application.log
    else
        log_error "日志文件不存在"
    fi
}

# 显示帮助
show_help() {
    echo "用法: $0 [选项] [命令]"
    echo
    echo "命令:"
    echo "  start          启动应用 (默认)"
    echo "  stop           停止应用"
    echo "  restart        重启应用"
    echo "  status         查看应用状态"
    echo "  logs           查看应用日志"
    echo "  build          仅构建项目"
    echo "  help           显示帮助信息"
    echo
    echo "选项:"
    echo "  --skip-tests   跳过测试，快速构建"
    echo "  --port PORT    指定端口 (默认: 8080)"
    echo "  --profile PROFILE  指定配置文件 (默认: dev)"
    echo
    echo "环境变量:"
    echo "  OPENAI_API_KEY      OpenAI API密钥"
    echo "  ANTHROPIC_API_KEY   Anthropic API密钥"
    echo "  DATABASE_URL        数据库连接URL"
    echo "  REDIS_URL           Redis连接URL"
    echo
    echo "示例:"
    echo "  $0 start --skip-tests"
    echo "  $0 start --port 9090"
    echo "  $0 restart --profile prod"
}

# 主函数
main() {
    # 解析参数
    local command="start"
    local skip_tests=false
    
    while [[ $# -gt 0 ]]; do
        case $1 in
            start|stop|restart|status|logs|build|help)
                command=$1
                shift
                ;;
            --skip-tests)
                skip_tests=true
                shift
                ;;
            --port)
                export SERVER_PORT=$2
                shift 2
                ;;
            --profile)
                export SPRING_PROFILES_ACTIVE=$2
                shift 2
                ;;
            *)
                log_error "未知参数: $1"
                show_help
                exit 1
                ;;
        esac
    done
    
    # 显示横幅
    show_banner
    
    # 执行命令
    case $command in
        start)
            check_requirements
            check_environment
            if [ "$skip_tests" = true ]; then
                build_project --skip-tests
            else
                build_project
            fi
            start_application
            ;;
        stop)
            stop_application
            ;;
        restart)
            restart_application
            ;;
        status)
            show_status
            ;;
        logs)
            show_logs
            ;;
        build)
            check_requirements
            if [ "$skip_tests" = true ]; then
                build_project --skip-tests
            else
                build_project
            fi
            ;;
        help)
            show_help
            ;;
        *)
            log_error "未知命令: $command"
            show_help
            exit 1
            ;;
    esac
}

# 捕获中断信号
trap 'log_info "收到中断信号，正在停止..."; stop_application; exit 0' INT TERM

# 运行主函数
main "$@"