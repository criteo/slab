// @flow
import { createRouter } from 'redux-url';
import { createBrowserHistory } from 'history';

const routes = {
  '/': 'GOTO_BOARDS',
  '/:board': 'GOTO_BOARD',
  '*': 'NOT_FOUND'
};

const router = createRouter(routes, createBrowserHistory());

export default router;