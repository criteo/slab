// @flow
import { Component } from 'react';
import { connect } from 'react-redux';
import type { State, Board } from '../state';
import { fetchBoard } from '../actions';
import Graph from './Graph';
import { getCurrentBoardName } from '../utils';

type Props = {
  error: ?string,
  isLoading: boolean,
  fetchBoard: Function,
  board: ?Board
};

class App extends Component {
  props: Props;

  poller: ?number;

  constructor(props: Props) {
    super(props);
    this.poller = null;
  }

  render() {
    const { error, board } = this.props;
    if (error)
      return (
        <h1 style={ { color: '#C20', fontSize: '36px' } }>
          {error}
        </h1>
      );
    const boardName = getCurrentBoardName();
    if (boardName) {
      if (board)
        return <Graph board={board}/>;
      else
        return (
          <div>
            <header>
              <h1>Loading...</h1>
            </header>
          </div>
        );
    }
    else
      return (
        <h1 style={ { color: '#BABABA', fontSize: '36px' } }>
          board is not defined, please check the URL
        </h1>
      );
  }

  componentDidMount() {
    const board = getCurrentBoardName();
    if (board) {
      this.props.fetchBoard(board);
      setInterval(() => this.props.fetchBoard(board), 300000);
    }
  }

  componenWillUnmount() {
    if (this.poller)
      clearInterval(this.poller);
  }
}

const select = (state: State, ownProps: Props): Props => ({
  ...ownProps,
  error: state.error,
  isLoading: state.isLoading,
  board: state.board
});

const actions = (dispatch, ownProps): Props => ({
  ...ownProps,
  fetchBoard: board => dispatch(fetchBoard(board))
});

export default connect(select, actions)(App);