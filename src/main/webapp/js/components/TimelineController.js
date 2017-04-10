// @flow
import { PureComponent } from 'react';
import { connect } from 'react-redux';
import moment from 'moment';

import type { State } from '../state';

import { switchBoardView, fetchHistory } from '../actions';
import Calendar from './Calendar';
import { Button } from '../lib';

type Props = {
  boardName: string,
  date: ?string,
  isLiveMode: boolean,
  isLoading: boolean,
  error: ?string,
  selectedTimestamp: ?number,
  switchToLiveMode: () => void,
  fetchHistory: (date: ?string) => void
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
      switchToLiveMode,
      isLoading,
      error
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
            <Button onClick={this.handleTimelineResetClick}>
              LAST 24H
            </Button>}
          {isLoading && <i className="fa fa-circle-o-notch fa-spin" />}
          {error}
        </span>
        <span className="board">
          {isLiveMode
            ? 'LIVE'
            : `SNAPSHOT ${moment(selectedTimestamp).format('YYYY-MM-DD HH:mm')}`}
          {!isLiveMode && <Button onClick={switchToLiveMode}>RESET</Button>}
        </span>
        <Calendar
          isOpen={isCalendarOpen}
          selectedDay={selectedDay}
          onDayClick={this.handleDayClick}
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

  handleTimelineResetClick = () => {
    this.props.fetchHistory();
    this.setState({ selectedDay: new Date(), isCalendarOpen: false });
  };
}

const select = (state: State) => ({
  date: state.history.date,
  isLiveMode: state.isLiveMode,
  selectedTimestamp: state.selectedTimestamp,
  isLoading: state.history.isLoading,
  error: state.history.error,
  boardName: state.currentBoard
});

const actions = dispatch => ({
  switchToLiveMode: () => dispatch(switchBoardView(true)),
  fetchHistory: function(date) {
    const props = this;
    return dispatch(fetchHistory(props.boardName, date));
  }
});

export default connect(select, actions)(TimelineController);
