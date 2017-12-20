// @flow
import {PureComponent} from 'react';
import {connect} from 'react-redux';
import moment from 'moment';
import numeral from 'numeral';
import classNames from 'classnames';
import Datetime from 'react-datetime';
import {BarChart, Bar, XAxis, YAxis, Tooltip, Label, Brush, ReferenceLine, ResponsiveContainer} from 'recharts';

import type {Stats, State, StatsGroup} from '../state';
import { fetchStats } from '../actions';

type CalendarProps = {
    boardName: string,
    isOpen: boolean,
    selectedDay: Date,
    stats: StatsGroup,
    isLoading: boolean,
    onDayClick: Function,
    onCloseClick: Function,
    fetchStats: Function,
    slo: number
};

type DatetimeProps = {
    className: string,
};

type Payload = {
    value: any,
}
const fullDateFormat = 'YYYY-MM-DD';

type CustomChartTooltipProps = {
    active: boolean,
    payload: Array<Payload>,
    label: string,
};
function CustomChartTooltip(props: CustomChartTooltipProps){
  const { active } = props;
  if (active) {
    const { payload, label } = props;
    return (
      <div className="custom-chart-tooltip">
          <div className="label-value">
              <span className="label">{moment(label).format(fullDateFormat)}</span>:<br/>
              <span className="value">{numeral(payload[0].value).format('0.00%')}</span>
          </div>
      </div>
    );
  }
  return null;
}

type ThresholdFillBarProps = {
    x: number,
    y: number,
    width: number,
    height: number,
    value: number,
    threshold: number,
};
function ThresholdFillBar(props: ThresholdFillBarProps) {
  const getPath = (x, y, width, height) => `M${x},${y} h ${width} v ${height} h -${width} Z`;
  const { x, y, width, height, value, threshold } = props;

  const pathClassName = classNames({
    'recharts-rectangle': true,
    good: value && threshold && value >= threshold,
    bad: value && threshold && value < threshold,
  });

  return <path d={getPath(x, y, width, height)} className={pathClassName}/>;
}

class Calendar extends PureComponent {
  props: CalendarProps;
  state = {
    flipped: false,
  };

  constructor(props: CalendarProps) {
    super(props);
    this.props.fetchStats();
  }

  render() {
    const buildDomain = (data, slo) => {
      const values = data && data.length > 0 && data.map(x => x.percent) || [0, 1];
      const [min, max] = [Math.min(...values), Math.max(...values)];
      return data && data.length > 1 && min !== max
        ? min > slo * 0.9 ? [slo * 0.9, 1] : ['auto', 1]
        : [0, 1];
    };

    const {isOpen, selectedDay, onDayClick, onCloseClick, stats, slo} = this.props;
    const {flipped} = this.state;

    const data = stats && Object.keys(stats.daily)
      .map(key => ({date: Number(key), percent: stats.daily[Number(key)]}))
      .sort((a, b) => a.date - b.date);

    const wrapperClass = classNames({
      'calendar-chart-wrapper': true,
      closed: !isOpen,
      flipped: flipped,
    });

    const flipperClass = classNames({
      flipper: true,
      flipped: flipped,
    });

    const flipViewButtonClass = classNames({
      'flip-view-button': true,
      chart: !flipped,
      calendar: flipped,
    });

    return (
      <div className={wrapperClass}>
          <div className={flipperClass}>
              <Datetime className={'front'}
                        value={selectedDay}
                        input={false} timeFormat={false}
                        renderDay={this.renderDay(stats && stats.daily, slo)}
                        renderMonth={this.renderMonth(stats && stats.monthly, slo)}
                        renderYear={this.renderYear(stats && stats.yearly, slo)}
                        onChange={onDayClick}
                        isValidDate={this.validateDate}
              />
              { data && data.length > 0 &&
                (<ResponsiveContainer className="back" width="100%" height="100%">
                    <BarChart data={data} margin={{left: -10, right:20}}>
                        <XAxis dataKey="date" tickFormatter={this.xAxisTickFormatter}/>
                        <YAxis domain={buildDomain(data, slo)}
                               tickFormatter={this.yAxisTickFormatter}/>
                        <Tooltip content={<CustomChartTooltip payload label active/>}/>
                        <Brush dataKey='date' height={30} tickFormatter={ _ => '' }
                               startIndex={Math.max(0, data.length - 31)}
                               endIndex={Math.max(0, data.length - 1)}
                               travellerWidth={10}/>
                        <Bar dataKey="percent" shape={<ThresholdFillBar y x width height value threshold={slo}/>}/>
                      <ReferenceLine y={slo} alwaysShow isFront>
                        <Label value={this.formatPercentage(slo)} offset={5} position="left" />
                        <Label value="SLO" offset={-5} position="right" />
                      </ReferenceLine>
                    </BarChart>
                </ResponsiveContainer>)
              }
            </div>

            <button className="close-button" onClick={onCloseClick}>×</button>
            { data && data.length > 0 &&
              (<button className={flipViewButtonClass} onClick={this.onFlipView}>{flipped}</button>)
            }
      </div>
    );
  }

  formatPercentage = (val: number): string => numeral(val).format(val < 1 ? '0.00%' : '0%');

  getDateInfo(stats, timestamp, slo): [string, string] {
    const percentage = stats && stats[timestamp];
    const percentageFormatted = !stats || isNaN(percentage)
      ? 'N/A'
      : this.formatPercentage(percentage);
    const dateClass = classNames({
      'data-available': percentage && !isNaN(percentage),
      'data-unavailable': !percentage || isNaN(percentage),
      good: percentage && percentage >= slo,
      bad: percentage && percentage < slo
    });
    return [percentageFormatted, dateClass];
  }

  renderDay = (stats: Stats, slo: number) => ( props: DatetimeProps, currentDate: moment) => {
    const timestamp = currentDate.valueOf();
    const [percentageFormatted, dateClass] = this.getDateInfo(stats, timestamp, slo);

    props.className += ` ${dateClass}`;
    return <td {...props}>
      <span className="date-label"> { numeral(currentDate.date()).format('00') } </span>
      <span className="percent-label"> { percentageFormatted } </span>
    </td>;
  };

  renderMonth = (stats: Stats, slo: number) => ( props: DatetimeProps, month: number, year: number) => {
    const timestamp = moment().date(1).month(month).year(year).startOf('day').valueOf();
    const [percentageFormatted, dateClass] = this.getDateInfo(stats, timestamp, slo);

    const localMoment = moment();
    const shortMonthName = localMoment.localeData().monthsShort(localMoment.month(month)).substring(0, 3);

    props.className += ` ${dateClass}`;
    return <td {...props}>
      <span className="date-label"> { shortMonthName } </span>
      <span className="percent-label"> { percentageFormatted } </span>
    </td>;
  };

  renderYear = (stats: Stats, slo: number) => ( props: DatetimeProps, year: number) => {
    const timestamp = moment().date(1).month(0).year(year).startOf('day').valueOf();
    const [percentageFormatted, dateClass] = this.getDateInfo(stats, timestamp, slo);

    props.className += ` ${dateClass}`;
    return <td {...props}>
      <span className="date-label"> { year } </span>
      <span className="percent-label"> { percentageFormatted } </span>
    </td>;
  };

  validateDate = (currentDate: moment) => {
    const tomorrow = moment().startOf('day').add(1, 'day');
    return currentDate.isBefore(tomorrow);
  };

  onFlipView = () => this.setState({flipped: !this.state.flipped});

  xAxisTickFormatter = (val) => moment(val).format(fullDateFormat);

  yAxisTickFormatter = (val) => numeral(val).format('0%');
}

const select = (state: State) => ({
  stats: state.stats.data,
  isLoading: state.stats.isLoading,
  error: state.stats.error,
  boardName: state.currentBoard,
});

const actions = (dispatch) => ({
  fetchStats: function() {
    const props = this;
    return dispatch(fetchStats(props.boardName));
  }
});

export default connect(select, actions)(Calendar);
