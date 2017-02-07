import { createStore, compose, applyMiddleware } from 'redux';
import createSagaMiddlware from 'redux-saga';
import reducer from './state';
import rootSaga from './sagas';

export default function configureStore() {
  const sagaMiddleware = createSagaMiddlware();
  const store = createStore(
    reducer,
      compose(
      applyMiddleware(
        sagaMiddleware
      ),
      window.devToolsExtension ? window.devToolsExtension() : _ => _
    )
  );
  sagaMiddleware.run(rootSaga);

  if (module.hot) {
    module.hot.accept(() => {
      store.replaceReducer(require('./state').default);
      return true;
    });
  }

  return store;
}