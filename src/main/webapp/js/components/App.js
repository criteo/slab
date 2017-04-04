// @flow
import { Component } from 'react';
import { connect } from 'react-redux';
import moment from 'moment';
import type { State, Route, BoardView } from '../state';
import { switchBoardView } from '../actions';
import Graph from './Graph';
import BoardList from './BoardList';

type Props = {
  isLoading: boolean,
  board: ?BoardView,
  error: ?string,
  route: Route,
  isLiveMode: boolean,
  timestamp: ?number,
  switchToLiveMode: () => void
};

class App extends Component {
  props: Props;

  constructor(props: Props) {
    super(props);
  }

  render() {
    const { error, board, route, isLiveMode, timestamp } = this.props;
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
            <div className="time-indicator">
              {
                isLiveMode ?
                'LIVE':
                <span>
                  {`SNAPSHOT ${moment(timestamp).format('YYYY-MM-DD HH:mm')}`}
                  <button onClick={this.switchToLiveMode}><i className="fa fa-undo"></i></button>
                </span>
              }
            </div>
            <Graph
              board={board}
              isLiveMode={isLiveMode}
              timestamp={timestamp}
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

  switchToLiveMode = () => {
    this.props.switchToLiveMode();
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

const actions = (dispatch, ownProps): Props => ({
  ...ownProps,
  switchToLiveMode: () => dispatch(switchBoardView(true))
});

export default connect(select, actions)(App);
