import { defineConfig } from 'vitepress'
import { currentVersion, versions } from './versions.mts'

export default defineConfig({
  lang: 'en-US',
  title: 'PeakClient',
  description: 'Server-driven HUD and menus for Minecraft 1.21.1 on Fabric and NeoForge.',
  base: process.env.DOCS_BASE ?? '/',
  cleanUrls: true,
  themeConfig: {
    nav: [
      { text: 'Getting Started', link: '/getting-started' },
      { text: 'Concepts', link: '/concepts/documents-and-elements' },
      { text: 'Elements', link: '/elements/overview' },
      { text: 'Menus', link: '/menus/overview' },
      { text: 'Reference', link: '/reference/built-in-placeholders' },
      {
        text: `v${currentVersion}`,
        items: versions.map((entry) => ({
          text: entry.version === currentVersion ? `${entry.version} (current)` : entry.version,
          link: entry.link
        }))
      }
    ],
    sidebar: [
      {
        text: 'Introduction',
        items: [
          { text: 'Getting Started', link: '/getting-started' },
          { text: 'Examples', link: '/examples' }
        ]
      },
      {
        text: 'Concepts',
        items: [
          { text: 'Documents and Elements', link: '/concepts/documents-and-elements' },
          { text: 'Sending to Players', link: '/concepts/sending' },
          { text: 'Updating Elements', link: '/concepts/updates' },
          { text: 'Values and Placeholders', link: '/concepts/values-and-placeholders' },
          { text: 'Threads and Timing', link: '/concepts/threads-and-timing' }
        ]
      },
      {
        text: 'Elements',
        items: [
          { text: 'Overview', link: '/elements/overview' },
          { text: 'Text', link: '/elements/text' },
          { text: 'Images, Items and Players', link: '/elements/media' },
          { text: 'Progress and Charts', link: '/elements/progress-and-charts' },
          { text: 'Maps and Navigation', link: '/elements/maps-and-navigation' },
          { text: 'Layout and Containers', link: '/elements/layout' },
          { text: 'Animation and Displays', link: '/elements/animation-and-displays' }
        ]
      },
      {
        text: 'Styling',
        items: [
          { text: 'Colors and Gradients', link: '/styling/colors-and-gradients' },
          { text: 'Box Style', link: '/styling/box-style' }
        ]
      },
      {
        text: 'Interaction',
        items: [
          { text: 'Events', link: '/interaction/events' }
        ]
      },
      {
        text: 'Menus',
        items: [
          { text: 'Overview', link: '/menus/overview' },
          { text: 'Input Elements', link: '/menus/inputs' },
          { text: 'Templates', link: '/menus/templates' },
          { text: 'Server Events', link: '/menus/server-events' },
          { text: 'Theme', link: '/menus/theme' }
        ]
      },
      {
        text: 'Vanilla HUD',
        items: [
          { text: 'Vanilla HUD', link: '/vanilla-hud' }
        ]
      },
      {
        text: 'Reference',
        items: [
          { text: 'Built-in Placeholders', link: '/reference/built-in-placeholders' },
          { text: 'Naming and IDs', link: '/reference/naming-and-ids' },
          { text: 'Pitfalls', link: '/reference/pitfalls' },
          { text: 'Troubleshooting', link: '/reference/troubleshooting' },
          { text: 'Compatibility', link: '/reference/compatibility' },
          { text: 'Changelog', link: '/reference/changelog' }
        ]
      }
    ],
    socialLinks: [
      { icon: 'github', link: 'https://github.com/GekidoLukas/peakclient' }
    ],
    editLink: {
      pattern: 'https://github.com/GekidoLukas/peakclient/edit/main/docs/:path',
      text: 'Edit this page on GitHub'
    },
    search: {
      provider: 'local'
    },
    footer: {
      message: `PeakClient documentation v${currentVersion}. Licensed under the <a href="https://github.com/GekidoLukas/peakclient/blob/main/LICENSE">GNU GPL v3.0</a>.`
    }
  },
  sitemap: {
    hostname: 'https://gekidolukas.github.io/peakclient/'
  }
})
