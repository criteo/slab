var path = require('path');
var yargs = require('yargs').argv;
var webpack = require('webpack');
var HtmlWebpackPlugin = require('html-webpack-plugin');
var CopyWebpackPlugin = require('copy-webpack-plugin');

yargs.env = yargs.env || {};
var baseDir = path.resolve(__dirname, 'src/main/webapp');
var outPath = path.resolve(__dirname, yargs.env.out || 'dist');
var isProdMode = yargs.p || false;
var config = {
  entry: [
    'whatwg-fetch',
    'babel-polyfill',
    path.resolve(baseDir, 'js/index.js'),
    path.resolve(baseDir, 'style/index.styl')
  ],
  output: {
    path: outPath,
    filename: 'index.js',
    publicPath: '/'
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
            'transform-object-rest-spread',
            'transform-exponentiation-operator'
          ]
        },
        include: [path.join(__dirname, 'src')]
      },
      {
        test: /\.styl/,
        loaders: ['style-loader', 'css-loader', 'stylus-loader']
      },
      {
        test: /\.png$/,
        loaders: ['file-loader']
      }
    ]
  },
  resolve: {
    alias: {
      'src': path.resolve(__dirname, 'src/main/webapp/js') // for testing
    }
  },
  plugins: [
    new HtmlWebpackPlugin({
      filename: 'index.html',
      template: path.resolve(baseDir, 'public/index.ejs'),
      inject: false,
      title: 'Slab'
    }),
    new CopyWebpackPlugin(
      [
        {
          from: path.resolve(baseDir, 'public'),
          to: 'public'
        }
      ],
      {
        ignore: [
          '*.ejs'
        ]
      }
    ),
    new webpack.ProvidePlugin({
      'React': 'react'
    }),
    new webpack.NamedModulesPlugin()
  ],
  devServer: {
    contentBase: outPath,
    compress: true,
    port: 9001,
    historyApiFallback: true,
    proxy: {
      '/api': 'http://localhost:' + (yargs.env.serverPort || 8080)
    },
    hot: !isProdMode
  },
  devtool: isProdMode ? 'source-map' : 'eval-source-map'
};

if (!isProdMode) {
  config.plugins.push(new webpack.NamedModulesPlugin());
  config.plugins.push(new webpack.HotModuleReplacementPlugin());
}

module.exports = config;