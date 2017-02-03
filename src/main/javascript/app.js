/* @flow */

import React from 'react'
import { connect } from 'react-redux'
import { findDOMNode } from 'react-dom'
import Graph from './graph'
import * as Actions from './actions'

import type { State } from  './state'

type Props = {
}

class App extends React.Component {
  props: Props;

  constructor(props: Props) {
    super(props);
  }

  render() {
    return (
      <Graph />
    )
  }
}

function actions(dispatch, ownProps: Props) {
  return {
    ...ownProps
  }
}

function select(state: State, ownProps: Props): Props {
  return {
    ...ownProps
  }
}

export default connect(select, actions)(App)
