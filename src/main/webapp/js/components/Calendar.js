// @flow
import { PureComponent } from 'react';
import DayPicker from 'react-day-picker';
import { connect } from 'react-redux';
import moment from 'moment';

import type { Stats, State } from '../state';
import { fetchStats } from '../actions';

type Props = {
  boardName: string,
  isOpen: boolean,
  selectedDay: Date,
  onDayClick: Function,
  stats: {
    [k: ?number]: Stats
  },
  fetchStats: Function
};

class Calendar extends PureComponent {
  props: Props;

  constructor(props: Props) {
    super(props);
  }

  render() {
    const { isOpen, selectedDay, onDayClick } = this.props;
    return (
      <DayPicker
        className={isOpen ? '' : 'hidden'}
        selectedDays={selectedDay}
        disabledDays={{
          after: moment().startOf('day').toDate()
        }}
        onDayClick={ onDayClick }
        renderDay={ this.renderDay }
      />
    );
  }

  componentWillReceiveProps(nextProps) {
    if (this.props.isOpen === false && nextProps.isOpen) {
      this.props.fetchStats();
    }
  }

  renderDay = (day: Date) => {
    const normalized = day.valueOf() - day.valueOf() % 86400000;
    const stats = this.props.stats && this.props.stats[normalized];
    return (
      <div>
        <div>{day.getDate()}</div>
        { stats &&
          <div style={ { 'fontSize': '10px'} } >
            { showStatsPercentage(stats) }
          </div>
        }
      </div>
    );
  };
}

const showStatsPercentage = (stats: Stats): string =>
  // rate of known non-errors
  stats.total - stats.unknown === 0 ?
  'N/A' :
  (100 - 100 * stats.errors / (stats.total - stats.unknown)).toFixed(2) + '%';

const select = (state: State) => ({
  stats: state.stats.data,
  isLoading: state.stats.isLoading,
  error: state.stats.error,
  boardName: state.currentBoard
});

const actions = (dispatch) => ({
  fetchStats: function() {
    const props = this;
    return dispatch(fetchStats(props.boardName));
  }
});

export default connect(select, actions)(Calendar);