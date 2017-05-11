// @flow
import { createRouter } from 'redux-url';
import { createBrowserHistory } from 'history';

import type { Action } from './actions';
const routes = {
  '/': 'GOTO_BOARDS',
  '/:board': ({ board }): Action => ({
    type: 'GOTO_LIVE_BOARD',
    board
  }),
  '/:board/snapshot/:timestamp': ({ board, timestamp }): Action => ({
    type: 'GOTO_SNAPSHOT',
    board,
    timestamp: parseInt(timestamp)
  }),
  '*': 'NOT_FOUND'
};

const router = createRouter(routes, createBrowserHistory());

export default router;