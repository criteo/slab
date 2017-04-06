// @flow
import { Component } from 'react';
import { connect } from 'react-redux';
import type { State, Route, BoardView } from '../state';
import Graph from './Graph';
import BoardList from './BoardList';

type Props = {
  isLoading: boolean,
  board: ?BoardView,
  error: ?string,
  route: Route
};

class App extends Component {
  props: Props;

  constructor(props: Props) {
    super(props);
  }

  render() {
    const { error, board, route } = this.props;
    if (error)
      return (
        <h1 style={{ color: '#C20', fontSize: '36px' }}>
          {error}
        </h1>
      );
    if (route.path === 'BOARDS') return <BoardList />;
    if (route.path === 'BOARD') {
      if (board)
        return (
          <div>
            <Graph
              board={board}
            />
          </div>
        );
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
        <h1 style={{ color: '#BABABA', fontSize: '36px' }}>
          Not found
        </h1>
      );
  }
}

const select = (state: State, ownProps: Props): Props => ({
  ...ownProps,
  error: state.selectedBoardView.error,
  isLoading: state.selectedBoardView.isLoading,
  board: state.selectedBoardView.data,
  route: state.route,
  isLiveMode: state.isLiveMode,
  timestamp: state.selectedTimestamp
});

export default connect(select)(App);
