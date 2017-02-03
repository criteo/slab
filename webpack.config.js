var path = require('path');
var yargs = require('yargs').argv;

module.exports = {
  entry: [
    'whatwg-fetch',
    path.resolve(__dirname, 'src/main/javascript/index.js')
  ],
  output: {
    path: path.resolve(__dirname, yargs.out),
    filename: 'index.js'
  },
  module: {
    loaders: [
      {
        test: /\.jsx?$/,
        loader: 'babel-loader',
        query: {
          cacheDirectory: true,
          presets: ['react', 'es2015'],
          plugins: [
            'transform-class-properties',
            'transform-object-rest-spread'
          ]
        },
        include: path.join(__dirname, 'src')
      }
    ]
  },
  devtool: 'cheap-module-source-map'
}
