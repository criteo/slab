// @flow
import { Component } from 'react';
import { connect } from 'react-redux';
import type { State, Route, BoardView } from '../state';
import Graph from './Graph';
import BoardList from './BoardList';
import ErrorPage from './ErrorPage';

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
      return <ErrorPage message={error}/>;
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
          <h1 style={{ color: '#BABABA' }} className="info">
            Loading...
          </h1>
        );
    } else
      return (
        <h1 style={{ color: '#BABABA' }} className="info">
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
