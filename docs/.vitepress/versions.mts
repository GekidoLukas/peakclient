import pkg from '../package.json'

export interface DocsVersion {
  version: string
  link: string
}

export const currentVersion: string = pkg.version

export const versions: DocsVersion[] = [
  { version: currentVersion, link: '/' }
]
