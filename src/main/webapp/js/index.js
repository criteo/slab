import React from 'react';
import { render } from 'react-dom';
import configureStore from './store';
import App from './components/App';
import { Provider } from 'react-redux';

const store = configureStore();

const mount = Component => render(
  <Provider store={ store }>
    <Component />
  </Provider>,
  document.getElementById('app')
);

mount(App);

if (module.hot) {
  module.hot.accept('./components/App',() => {
    mount(require('./components/App').default);
    return true;
  });
}