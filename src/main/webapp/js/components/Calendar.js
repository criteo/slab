// @flow
import { PureComponent } from 'react';
import DayPicker from 'react-day-picker';
import { connect } from 'react-redux';
import moment from 'moment';

import type { Stats, StatsEntry, State } from '../state';
import { fetchStats } from '../actions';
import { aggregateStatsByDay } from '../utils';

type Props = {
  boardName: string,
  isOpen: boolean,
  selectedDay: Date,
  onDayClick: Function,
  stats: Stats,
  fetchStats: Function
};

class Calendar extends PureComponent {
  props: Props;

  constructor(props: Props) {
    super(props);
  }

  render() {
    const { isOpen, selectedDay, onDayClick, stats } = this.props;
    return (
      <DayPicker
        className={isOpen ? '' : 'hidden'}
        selectedDays={selectedDay}
        disabledDays={{
          after: moment().startOf('day').toDate()
        }}
        onDayClick={ onDayClick }
        renderDay={ this.renderDay(aggregateStatsByDay(stats)) }
      />
    );
  }

  componentWillReceiveProps(nextProps) {
    if (this.props.isOpen === false && nextProps.isOpen) {
      this.props.fetchStats();
    }
  }

  renderDay = (stats: Stats) => (day: Date) => {
    const normalized = moment(day).startOf('day').valueOf();
    const statsOfDay = stats && stats[normalized];
    return (
      <div>
        <div>{day.getDate()}</div>
        { statsOfDay &&
          <div style={ { 'fontSize': '10px'} } >
            { showStatsPercentage(statsOfDay) }
          </div>
        }
      </div>
    );
  };
}

export const showStatsPercentage = (stats: StatsEntry): string =>
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