// @flow
import { PureComponent } from 'react';
import { connect } from 'react-redux';
import moment from 'moment';

import type { State } from '../state';

import { fetchHistory, navigateToLiveBoard } from '../actions';
import Calendar from './Calendar';
import { Button } from '../lib';

type Props = {
  boardName: string,
  date: ?string,
  isLiveMode: boolean,
  isLoading: boolean,
  error: ?string,
  selectedTimestamp: ?number,
  navigateToLiveBoard: () => void,
  fetchHistory: (date: ?string) => void,
  isLoadingBoard: boolean,
  slo: number,
};

class TimelineController extends PureComponent {
  props: Props;
  state: {
    isCalendarOpen: boolean,
    selectedDay: Date
  };

  constructor(props: Props) {
    super(props);
    this.state = {
      isCalendarOpen: false,
      selectedDay: new Date()
    };
  }

  render() {
    const {
      date,
      isLiveMode,
      selectedTimestamp,
      isLoading,
      error,
      isLoadingBoard,
      slo
    } = this.props;
    const { isCalendarOpen, selectedDay } = this.state;
    return (
      <div id="controller">
        <span className="timeline">
          {date ? date : 'Last 24 hours'}
          <Button
            onClick={() =>
              this.setState({ isCalendarOpen: !this.state.isCalendarOpen })}
          >
            {isCalendarOpen ? 'CLOSE' : 'CALENDAR'}
          </Button>
          {date &&
            <Button onClick={this.handleLast24HClick}>
              LAST 24H
            </Button>}
          {isLoading && <i className="fa fa-circle-o-notch fa-spin" />}
          {error}
        </span>
        <span className="board">
          {isLiveMode
            ? 'LIVE'
            : `SNAPSHOT ${moment(selectedTimestamp).format('YYYY-MM-DD HH:mm')}`}
          {!isLiveMode && <Button onClick={this.hanldeResetClick}>RESET</Button>}
        </span>
        {
          isLoadingBoard && <i className="fa fa-circle-o-notch fa-spin" />
        }
        <Calendar
          isOpen={isCalendarOpen}
          selectedDay={selectedDay}
          onDayClick={this.handleDayClick}
          onCloseClick={this.handleCloseClick}
          slo={slo}
        />
      </div>
    );
  }

  handleDayClick = selectedDay => {
    const { selectedDay: prevDay } = this.state;
    this.setState({ selectedDay, isCalendarOpen: false }, () => {
      if (prevDay - selectedDay !== 0)
        this.props.fetchHistory(moment(this.state.selectedDay).format('YYYY-MM-DD'));
    });
  };

  handleLast24HClick = () => {
    this.props.fetchHistory();
    this.setState({ selectedDay: new Date(), isCalendarOpen: false });
  };

  hanldeResetClick = () => {
    this.props.navigateToLiveBoard();
  };

  handleCloseClick = () => {
    this.setState({ isCalendarOpen: false });
  };
}

const select = (state: State) => ({
  date: state.history.date,
  isLiveMode: state.isLiveMode,
  selectedTimestamp: state.selectedTimestamp,
  isLoading: state.history.isLoading,
  error: state.history.error,
  boardName: state.currentBoard,
  isLoadingBoard: state.selectedBoardView.isLoading,
  slo: state.selectedBoardView.data && state.selectedBoardView.data.slo
});

const actions = dispatch => ({
  navigateToLiveBoard: function() {
    const props = this;
    return dispatch(navigateToLiveBoard(props.boardName));
  },
  fetchHistory: function(date) {
    const props = this;
    return dispatch(fetchHistory(props.boardName, date));
  }
});

export default connect(select, actions)(TimelineController);
