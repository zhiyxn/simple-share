const { createApp } = require('vue')
const { build } = require('vite')

console.log('Starting build test...')

build({
  configFile: './vite.config.ts',
  mode: 'production'
}).then(() => {
  console.log('Build completed successfully!')
  process.exit(0)
}).catch(error => {
  console.error('Build failed:', error)
  process.exit(1)
})
