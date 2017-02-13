var webpackConfig = require('./webpack.config');
module.exports = function(config) {
  config.set({
    browsers: ['PhantomJS'],
    singleRun: false,
    frameworks: ['mocha', 'chai'],
    files: [
      'node_modules/babel-polyfill/dist/polyfill.js',
      {
        pattern: 'src/test/webapp/**/*.spec.js',
        watched: false,
        included: true,
        served: true
      }
    ],
    preprocessors: {
      'src/**/*.js': ['webpack', 'sourcemap']
    },
    webpack: webpackConfig,
    webpackMiddleware: {
      noInfo: true
    },
    reporters: ['progress'],
    color: true,
    autoWatch: true
  });
};
