/* @flow */

import React from 'react'
import { Provider } from 'react-redux'
import { createStore, applyMiddleware, compose } from 'redux'
import { render } from 'react-dom'
import { initialState, reducers } from  './state'
import * as Actions from './actions'
import App from './app'

import type { State, Board } from './state'
import type { Action } from './actions'

const store = createStore(
  reducers,
  initialState(location.pathname + location.search),
  compose(
    window.devToolsExtension ? window.devToolsExtension() : _ => _
  )
)

const board = /[/]([-_a-zA-Z0-9]+)/.exec(document.location.pathname)[1]

let refresh = () => {
  store.dispatch(Actions.loading())
  fetch(`/${board}/checks`).then(res => res.json()).then(
    (board: Board) => {
      store.dispatch(Actions.loaded(board))
    },
    error => {
      console.error(error)
      store.dispatch(Actions.cantLoad())
    }
  ).then(_ => {
    setTimeout(refresh, 30000)
  })
}
refresh()

render(
  <Provider store={store}>
    <App />
  </Provider>,
  document.getElementById('app')
)
