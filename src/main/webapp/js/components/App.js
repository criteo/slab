// @flow
import { Component } from 'react';
import { connect } from 'react-redux';
import type { State, Board, Route } from '../state';
import { fetchBoard } from '../actions';
import Graph from './Graph';
import BoardList from './BoardList';

type Props = {
  error: ?string,
  isLoading: boolean,
  fetchBoard: Function,
  board: ?Board,
  route: Route
};

class App extends Component {
  props: Props;

  poller: ?number;

  static POLLING_INTERVAL_SECONDS = 30;

  constructor(props: Props) {
    super(props);
    this.poller = null;
  }

  render() {
    const { error, board, route } = this.props;
    if (error)
      return (
        <h1 style={ { color: '#C20', fontSize: '36px' } }>
          {error}
        </h1>
      );
    if (route.path === 'BOARDS')
      return <BoardList />;
    if (route.path === 'BOARD') {
      if (board)
        return <Graph board={board} />;
      else
        return (
          <div>
            <header>
              <h1>Loading...</h1>
            </header>
          </div>
        );
    } else
      return (
        <h1 style={ { color: '#BABABA', fontSize: '36px' } }>
          Not found
        </h1>
      );
  }

  componentDidMount() {
    this.reload();
  }

  componenWillUnmount() {
    clearTimeout(this.poller);
  }

  reload() {
    const { route } = this.props;
    if (route.board) {
      this.props.fetchBoard(route.board);
      clearTimeout(this.poller);
      this.poller = setTimeout(() => this.reload(), App.POLLING_INTERVAL_SECONDS * 1000);
    }
  }
}

const select = (state: State, ownProps: Props): Props => ({
  ...ownProps,
  error: state.error,
  isLoading: state.isLoading,
  board: state.board,
  route: state.route
});

const actions = (dispatch, ownProps): Props => ({
  ...ownProps,
  fetchBoard: board => dispatch(fetchBoard(board))
});

export default connect(select, actions)(App);