import { defineUserConfig } from 'vuepress'
import { viteBundler } from '@vuepress/bundler-vite'
import { plumeTheme } from 'vuepress-theme-plume'

export default defineUserConfig({
  base: '/',
  lang: 'zh-CN',
  title: 'RiverFlow',
  description: '可视化流程编排与政务数据交换平台',

  head: [
    ['link', { rel: 'icon', href: '/images/logo.png' }],
  ],

  bundler: viteBundler(),

  theme: plumeTheme({
    logo: '/images/logo.png',

    navbar: [
      { text: '首页', link: '/' },
      {
        text: '指南',
        items: [
          { text: '快速开始', link: '/guide/start/quickstart.html' },
          { text: '用户使用', link: '/guide/user/workflow-design.html' },
          { text: '开发指南', link: '/guide/developer/architecture.html' },
          { text: '插件系统', link: '/guide/plugin/develop-guide.html' },
          { text: '部署指南', link: '/guide/deploy/single-deploy.html' },
          { text: '常见问题', link: '/guide/faq/common-questions.html' },
        ],
      },
      {
        text: '更新日志',
        link: '/changelog/',
      },
    ],

    sidebar: {
      '/guide/start/': [
        {
          text: '快速开始',
          items: [
            { text: '项目介绍', link: '/guide/start/quickstart.html' },
          ],
        },
      ],
      '/guide/user/': [
        {
          text: '用户使用',
          items: [
            { text: '流程设计', link: '/guide/user/workflow-design.html' },
            { text: '节点类型', link: '/guide/user/node-types.html' },
            { text: '上下文变量', link: '/guide/user/context-variables.html' },
          ],
        },
      ],
      '/guide/developer/': [
        {
          text: '开发指南',
          items: [
            { text: '架构设计', link: '/guide/developer/architecture.html' },
            { text: 'API 参考', link: '/guide/developer/api-reference.html' },
          ],
        },
      ],
      '/guide/plugin/': [
        {
          text: '插件系统',
          items: [
            { text: '插件开发指南', link: '/guide/plugin/develop-guide.html' },
            { text: '插件使用指南', link: '/guide/plugin/use-guide.html' },
          ],
        },
      ],
      '/guide/deploy/': [
        {
          text: '部署指南',
          items: [
            { text: '单机部署', link: '/guide/deploy/single-deploy.html' },
            { text: '分布式部署', link: '/guide/deploy/distributed-deploy.html' },
          ],
        },
      ],
      '/guide/faq/': [
        {
          text: '常见问题',
          items: [
            { text: '常见问题汇总', link: '/guide/faq/common-questions.html' },
          ],
        },
      ],
    },

    footer: {
      message: 'Released under the MIT License.',
      copyright: 'Copyright © 2024-present RiverFlow Team',
    },
  }),
})
