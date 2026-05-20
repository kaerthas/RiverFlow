import MinioNodeConfig from './components/MinioNodeConfig.vue'

const pluginConfig = {
  minio: {
    name: 'MinIO文件推送',
    icon: 'CloudUpload',
    category: 'storage',
    component: MinioNodeConfig,
    description: 'MinIO对象存储操作，支持文件上传、下载、删除、查询等'
  }
}

export function getPluginNodeConfig(nodeType) {
  return pluginConfig[nodeType]
}

export function getAllPluginNodes() {
  return Object.keys(pluginConfig).map(key => ({
    type: key,
    ...pluginConfig[key]
  }))
}

export default pluginConfig
