var path = require('path');
var yargs = require('yargs').argv;
var HtmlWebpackPlugin = require('html-webpack-plugin');
var CopyWebpackPlugin = require('copy-webpack-plugin');

module.exports = {
  entry: [
    'whatwg-fetch',
    path.resolve(__dirname, 'src/main/javascript/index.js'),
    path.resolve(__dirname, 'src/main/style/index.styl')
  ],
  output: {
    path: path.resolve(__dirname, yargs.env.out),
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
      },
      {
        test: /\.styl/,
        loaders: ['style-loader', 'css-loader', 'stylus-loader']
      }
    ]
  },
  plugins: [
    new HtmlWebpackPlugin({
      filename: 'index.html',
      template: path.resolve(__dirname, 'src/main/public/index.ejs'),
      inject: false,
      title: 'Slab'
    }),
    new CopyWebpackPlugin([
      {
        from: path.resolve(__dirname, 'src/main/public'),
        to: 'public'
      }
    ],
    {
      ignore: [
        '*.ejs'
      ]
    })
  ],
  devtool: 'cheap-module-source-map'
}
