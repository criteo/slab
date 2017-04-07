// @flow
import { PureComponent } from 'react';
import DayPicker from 'react-day-picker';
import { connect } from 'react-redux';
import moment from 'moment';

import type { State } from '../state';

import { switchBoardView, fetchHistory } from '../actions';
import { Button } from '../lib';

type Props = {
  boardTitle: string,
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
    isDayPickerOn: boolean,
    selectedDay: Date
  };

  constructor(props: Props) {
    super(props);
    this.state = {
      isDayPickerOn: false,
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
    const { isDayPickerOn, selectedDay } = this.state;
    return (
      <div id="controller">
        <span className="timeline">
          {date ? date : 'Last 24 hours'}
          <Button
            onClick={() =>
              this.setState({ isDayPickerOn: !this.state.isDayPickerOn })}
          >
            {isDayPickerOn ? 'CLOSE' : 'CHANGE'}
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
        <DayPicker
          className={isDayPickerOn ? '' : 'hidden'}
          selectedDays={selectedDay}
          disabledDays={{
            after: new Date().setDate(new Date().getDate() - 1)
          }}
          onDayClick={this.handleDayClick}
        />
      </div>
    );
  }

  handleDayClick = selectedDay => {
    const { selectedDay: prevDay } = this.state;
    this.setState({ selectedDay, isDayPickerOn: false }, () => {
      if (prevDay - selectedDay !== 0)
        this.props.fetchHistory(
          moment(this.state.selectedDay).format('YYYY-MM-DD')
        );
    });
  };

  handleTimelineResetClick = () => {
    const { fetchHistory } = this.props;
    fetchHistory();
    this.setState({ selectedDay: new Date(), isDayPickerOn: false });
  }
}

const select = (state: State) => ({
  date: state.history.date,
  isLiveMode: state.isLiveMode,
  selectedTimestamp: state.selectedTimestamp,
  isLoading: state.history.isLoading,
  error: state.history.error
});

const actions = (dispatch, ownProps: Props) => ({
  switchToLiveMode: () => dispatch(switchBoardView(true)),
  fetchHistory: date => dispatch(fetchHistory(ownProps.boardTitle, date))
});

export default connect(select, actions)(TimelineController);
