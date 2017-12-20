import { combineViewAndLayout, aggregateStatsByDay, aggregateStatsByMonth, aggregateStatsByYear } from 'src/utils';
import moment from 'moment';

describe('api utils specs', () => {
  describe('combineLayoutAndView', () => {
    const view = {
      title: 'Board1',
      status: 'SUCCESS',
      message: 'msg',
      boxes: [{
        title: 'Box1',
        status: 'SUCCESS',
        message: 'msg1',
        checks: [{
          title: 'Check1',
          status: 'SUCCESS',
          message: 'msg11',
          label: 'l11'
        }]
      }, {
        title: 'Box2',
        status: 'WARNING',
        message: 'msg2',
        checks: [{
          title: 'Check2',
          status: 'WARNING',
          message: 'msg22',
          label: 'l22'
        }]
      }]
    };

    const links = [
      ['Box1', 'Box2']
    ];

    const layout = {
      columns: [
        {
          percentage: 50,
          rows: [{
            title: 'Zone 1',
            percentage: 100,
            boxes: [{
              title: 'Box1',
              description: 'desc1',
              labelLimit: 2
            }]
          }]
        },
        {
          percentage: 50,
          rows: [{
            title: 'Zone 2',
            percentage: 100,
            boxes: [{
              title: 'Box2',
              description: 'desc2',
              labelLimit: 0
            }]
          }]
        }
      ]
    };

    it('combines board layout and view data, returns an object representing the view', () => {
      const result = combineViewAndLayout(view, layout, links);
      expect(result).to.deep.equal({
        title: 'Board1',
        status: 'SUCCESS',
        message: 'msg',
        columns: [
          {
            percentage: 50,
            rows: [{
              title: 'Zone 1',
              percentage: 100,
              boxes: [{
                title: 'Box1',
                description: 'desc1',
                status: 'SUCCESS',
                message: 'msg1',
                labelLimit: 2,
                checks: [{
                  title: 'Check1',
                  status: 'SUCCESS',
                  message: 'msg11',
                  label: 'l11'
                }]
              }]
            }]
          },
          {
            percentage: 50,
            rows: [{
              title: 'Zone 2',
              percentage: 100,
              boxes: [{
                title: 'Box2',
                status: 'WARNING',
                description: 'desc2',
                message: 'msg2',
                labelLimit: 0,
                checks: [{
                  title: 'Check2',
                  status: 'WARNING',
                  message: 'msg22',
                  label: 'l22'
                }]
              }]
            }]
          }
        ],
        links,
        slo: 0.97
      });
    });
  });

  describe('aggregateStatsByDay', () => {
    it('takes hourly stats and aggregate them into daily buckets', () => {
      const stats = {
        [moment('2000-01-01 00:00').valueOf()]: 0.5,
        [moment('2000-01-01 23:00').valueOf()]: 0.9,
        [moment('2000-01-02 00:00').valueOf()]: 1,
        [moment('2000-01-02 23:59').valueOf()]: 1,
      };
      const res = aggregateStatsByDay(stats);
      expect(res).to.deep.equal(
        {
          [moment('2000-01-01 00:00').valueOf()]: 0.7,
          [moment('2000-01-02 00:00').valueOf()]: 1,
        }
      );
    });
  });

  describe('aggregateStatsByMonth', () => {
    it('takes hourly stats and aggregate them into monthly buckets', () => {
      const stats = {
        [moment('2000-01-01 00:00').valueOf()]: 0.5,
        [moment('2000-01-01 23:00').valueOf()]: 0.9,
        [moment('2000-01-02 00:00').valueOf()]: 1,
        [moment('2000-01-02 23:59').valueOf()]: 1,
        [moment('2000-02-01 12:00').valueOf()]: 0.3,
        [moment('2000-02-02 13:00').valueOf()]: 0.3,
      };
      const res = aggregateStatsByMonth(stats);
      expect(res).to.deep.equal(
        {
          [moment('2000-01-01 00:00').valueOf()]: 0.85,
          [moment('2000-02-01 00:00').valueOf()]: 0.3,
        }
      );
    });
  });

  describe('aggregateStatsByYear', () => {
    it('takes hourly stats and aggregate them into yearly buckets', () => {
      const stats = {
        [moment('2000-01-01 00:00').valueOf()]: 0.5,
        [moment('2000-01-01 23:00').valueOf()]: 0.9,
        [moment('2000-01-02 00:00').valueOf()]: 1,
        [moment('2000-01-02 23:59').valueOf()]: 1,
        [moment('2000-02-01 12:00').valueOf()]: 0.3,
        [moment('2000-03-02 13:00').valueOf()]: 0.33,
        [moment('2001-01-01 00:00').valueOf()]: 0.1,
        [moment('2001-03-01 23:00').valueOf()]: 0.89,
        [moment('2001-03-12 00:00').valueOf()]: 0.14,
        [moment('2001-04-01 23:59').valueOf()]: 1,
        [moment('2001-05-01 12:00').valueOf()]: 0.4,
        [moment('2002-01-31 13:00').valueOf()]: 0.72,
        [moment('2002-04-04 00:00').valueOf()]: 0.32,
      };
      const res = aggregateStatsByYear(stats);
      expect(res).to.deep.equal(
        {
          [moment('2000-01-01 00:00').valueOf()]: 0.6716666666666665,
          [moment('2001-01-01 00:00').valueOf()]: 0.506,
          [moment('2002-01-01 00:00').valueOf()]: 0.52,
        }
      );
    });
  });
});