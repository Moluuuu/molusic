# molusic

## 架构
- molusic
  - audio-common 提供通用的音频处理接口
  - audio-file 提供音频文件处理的接口，及存储
  - audio-processing 音频处理逻辑调用，真正处理音频的服务
  - feign-api 提供远程接口供模块直接调用
  - gateway 网关

## 使用说明

下载依赖后，配置数据库、端口，启动对应微服务即可