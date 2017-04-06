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
  selectedTimestamp: ?number,
  switchToLiveMode: () => void,
  fetchHistory: (date:?string) => void
};

class TimelineController extends PureComponent {
  props: Props;
  state: {
    isDayPickerOn: boolean,
    selectedDay: Date
  }

  constructor(props: Props) {
    super(props);
    this.state = {
      isDayPickerOn: false,
      selectedDay: new Date()
    };
  }

  render() {
    const { date, isLiveMode, selectedTimestamp, switchToLiveMode, fetchHistory } = this.props;
    const { isDayPickerOn, selectedDay } = this.state;
    return (
      <div id="controller">
        <span className='timeline'>
          {date ? date : 'Last 24 hours'}
          <Button
            onClick={ () => this.setState({ isDayPickerOn: ! this.state.isDayPickerOn })}
          >
            { isDayPickerOn ? 'CLOSE' : 'CHANGE' }
          </Button>
          {
            date &&
            <Button
              onClick={ () => fetchHistory() }
            >
            RESET
            </Button>
          }
        </span>
        <span className='board'>
          { 
            isLiveMode ?
            'LIVE' :
            <span>
              {`SNAPSHOT ${moment(selectedTimestamp).format('YYYY-MM-DD HH:mm')}`}
              <Button onClick={switchToLiveMode}>Reset</Button>
            </span>
          }
        </span>
        <DayPicker
          className={ isDayPickerOn ? '' : 'hidden' }
          selectedDays={ selectedDay }
          disabledDays={ {
            after: new Date()
          } }
          onDayClick={ this.handleDayClick }
        />
      </div>
    );
  }

  handleDayClick = selectedDay => {
    this.setState({ selectedDay, isDayPickerOn: false }, () => {
      // TODO: no-op when the day is the same
      this.props.fetchHistory(moment(this.state.selectedDay).format('YYYY-MM-DD'));
    });
  }
}

const select = (state: State) => ({
  date: state.history.date,
  isLiveMode: state.isLiveMode,
  selectedTimestamp: state.selectedTimestamp
});


const actions = (dispatch, ownProps: Props) => ({
  switchToLiveMode: () => dispatch(switchBoardView(true)),
  fetchHistory: date => dispatch(fetchHistory(ownProps.boardTitle, date))
});

export default connect(select, actions)(TimelineController);